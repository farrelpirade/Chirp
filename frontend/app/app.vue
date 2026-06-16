<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { marked } from 'marked'

const BACKEND_URL = 'http://localhost:8080/api'

// Auth State
const user = ref(null) // Stores logged-in user details { username, fullName, email, nomorTelpon }
const currentTab = ref('feed') // 'feed' | 'messages' | 'profile'
const authMode = ref('login') // 'login' | 'register'

// Auth Form Fields
const loginUsername = ref('')
const loginPassword = ref('')
const registerUsername = ref('')
const registerFullName = ref('')
const registerEmail = ref('')
const registerNomorTelpon = ref('')
const registerPassword = ref('')
const authError = ref('')

// Timeline State
const threads = ref([])
const newPostContent = ref('')
const activeFilter = ref('foryou') // 'foryou' | 'trending'
const searchKeyword = ref('')

// Bookmark State
const bookmarks = ref([])

// News State
const newsList = ref([])

// DM & Chatbot State
const chatbotMessages = ref([])
const newChatbotMessage = ref('')
const isChatbotTyping = ref(false)

// Profile Page & Interactions State
const likedPosts = ref([])
const repostedPosts = ref([])
const profileTab = ref('posts') // 'posts' | 'likes' | 'reposts' | 'edit'

// Profile Edit Fields
const profileFullName = ref('')
const profileEmail = ref('')
const profileNomorTelpon = ref('')
const profilePassword = ref('')
const profileSuccess = ref('')
const profileError = ref('')

// Active thread replies display state
const expandedThreadReplies = ref({}) // threadId -> boolean
const replyContents = ref({}) // threadId -> string
const selectedProfileUser = ref(null)
const profileThreads = ref([])

const loadLocalInteractions = () => {
  if (!selectedProfileUser.value) return
  likedPosts.value = JSON.parse(localStorage.getItem(`chirp_liked_${selectedProfileUser.value.username}`) || '[]')
  repostedPosts.value = JSON.parse(localStorage.getItem(`chirp_reposted_${selectedProfileUser.value.username}`) || '[]')
}

const loadProfileThreads = async () => {
  try {
    profileThreads.value = await $fetch(`${BACKEND_URL}/threads`)
  } catch (err) {
    console.error('Failed to load profile threads:', err)
  }
}

const myTweets = computed(() => {
  return profileThreads.value.filter(t => t.user?.username === selectedProfileUser.value?.username)
})

const myLikedTweets = computed(() => {
  const targetUsername = selectedProfileUser.value?.username
  if (!targetUsername) return []
  const liked = JSON.parse(localStorage.getItem(`chirp_liked_${targetUsername}`) || '[]')
  return profileThreads.value.filter(t => liked.includes(t.id))
})

const myRepostedTweets = computed(() => {
  const targetUsername = selectedProfileUser.value?.username
  if (!targetUsername) return []
  const reposted = JSON.parse(localStorage.getItem(`chirp_reposted_${targetUsername}`) || '[]')
  return profileThreads.value.filter(t => reposted.includes(t.id))
})

const viewUserProfile = async (targetUsername) => {
  try {
    const res = await $fetch(`${BACKEND_URL}/users/${targetUsername}`)
    selectedProfileUser.value = res
    currentTab.value = 'profile'
    profileTab.value = 'posts'
    loadLocalInteractions()
    await loadProfileThreads()
  } catch (err) {
    console.error('Failed to load user profile:', err)
  }
}

// Load user from local storage
onMounted(() => {
  const savedUser = localStorage.getItem('chirp_user')
  if (savedUser) {
    user.value = JSON.parse(savedUser)
    selectedProfileUser.value = user.value
    initializeProfileFields()
    loadTimeline()
    loadNews()
    loadChatbotHistory()
    loadLocalInteractions()
    loadProfileThreads()
  }
})

const initializeProfileFields = () => {
  if (user.value) {
    profileFullName.value = user.value.fullName
    profileEmail.value = user.value.email
    profileNomorTelpon.value = user.value.nomorTelpon || ''
    profilePassword.value = ''
  }
}

// Watchers
watch(searchKeyword, () => {
  loadTimeline()
})

watch(activeFilter, () => {
  loadTimeline()
})

const loadBookmarks = async () => {
  if (!user.value) return
  try {
    bookmarks.value = await $fetch(`${BACKEND_URL}/threads/bookmarks`, {
      query: { username: user.value.username }
    })
  } catch (err) {
    console.error('Failed to load bookmarks:', err)
  }
}

// Authentication API calls
const handleLogin = async () => {
  authError.value = ''
  try {
    const res = await $fetch(`${BACKEND_URL}/users/login`, {
      method: 'POST',
      body: {
        username: loginUsername.value,
        password: loginPassword.value
      }
    })
    user.value = res
    selectedProfileUser.value = res
    localStorage.setItem('chirp_user', JSON.stringify(res))
    initializeProfileFields()
    loadTimeline()
    loadNews()
    loadChatbotHistory()
    loadLocalInteractions()
  } catch (err) {
    authError.value = err.data?.error || 'Login failed. Check username and password.'
  }
}

const handleRegister = async () => {
  authError.value = ''
  try {
    const res = await $fetch(`${BACKEND_URL}/users/register`, {
      method: 'POST',
      body: {
        username: registerUsername.value,
        password: registerPassword.value,
        email: registerEmail.value,
        NomorTelpon: registerNomorTelpon.value,
        fullName: registerFullName.value
      }
    })
    user.value = res
    selectedProfileUser.value = res
    localStorage.setItem('chirp_user', JSON.stringify(res))
    initializeProfileFields()
    loadTimeline()
    loadNews()
    loadChatbotHistory()
    loadLocalInteractions()
  } catch (err) {
    authError.value = err.data?.error || 'Registration failed.'
  }
}

const handleLogout = () => {
  if (user.value) {
    $fetch(`${BACKEND_URL}/users/logout`, {
      method: 'POST',
      body: { username: user.value.username }
    }).catch(console.error)
  }
  user.value = null
  localStorage.removeItem('chirp_user')
  likedPosts.value = []
  repostedPosts.value = []
}

const checkUserError = (err) => {
  const errMsg = err.data?.error || err.message || ""
  if (errMsg.includes('User not found')) {
    handleLogout()
    if (typeof window !== 'undefined') {
      window.alert('Your session has expired or the database was reset. Please log in or register again.')
    }
    return true
  }
  return false
}

// Timeline API calls
const loadTimeline = async () => {
  try {
    let url = `${BACKEND_URL}/threads`
    const params = {}
    if (searchKeyword.value) {
      params.search = searchKeyword.value
    } else if (activeFilter.value) {
      params.filter = activeFilter.value
    }
    
    threads.value = await $fetch(url, { query: params })
  } catch (err) {
    console.error('Failed to load timeline:', err)
  }
}

const handleCreatePost = async () => {
  if (!newPostContent.value.trim()) return
  try {
    await $fetch(`${BACKEND_URL}/threads`, {
      method: 'POST',
      body: {
        username: user.value.username,
        konten: newPostContent.value
      }
    })
    newPostContent.value = ''
    loadTimeline()
  } catch (err) {
    console.error('Failed to post thread:', err)
    checkUserError(err)
  }
}

const handleLike = async (id) => {
  try {
    await $fetch(`${BACKEND_URL}/threads/${id}/like`, { method: 'POST' })
    if (user.value) {
      if (!likedPosts.value.includes(id)) {
        likedPosts.value.push(id)
        localStorage.setItem(`chirp_liked_${user.value.username}`, JSON.stringify(likedPosts.value))
      }
    }
    if (currentTab.value === 'bookmarks') {
      loadBookmarks()
    } else {
      loadTimeline()
    }
  } catch (err) {
    console.error('Failed to like post:', err)
    checkUserError(err)
  }
}

const handleRepost = async (id) => {
  try {
    await $fetch(`${BACKEND_URL}/threads/${id}/repost`, { method: 'POST' })
    if (user.value) {
      if (!repostedPosts.value.includes(id)) {
        repostedPosts.value.push(id)
        localStorage.setItem(`chirp_reposted_${user.value.username}`, JSON.stringify(repostedPosts.value))
      }
    }
    if (currentTab.value === 'bookmarks') {
      loadBookmarks()
    } else {
      loadTimeline()
    }
  } catch (err) {
    console.error('Failed to repost thread:', err)
    checkUserError(err)
  }
}

const handleBookmark = async (id) => {
  try {
    await $fetch(`${BACKEND_URL}/threads/${id}/bookmark`, {
      method: 'POST',
      body: { username: user.value.username }
    })
    if (currentTab.value === 'bookmarks') {
      loadBookmarks()
    } else {
      loadTimeline()
    }
  } catch (err) {
    console.error('Failed to bookmark thread:', err)
    checkUserError(err)
  }
}

const handlePostReply = async (threadId) => {
  const content = replyContents.value[threadId]
  if (!content || !content.trim()) return
  
  try {
    const thread = threads.value.find(t => t.id === threadId) || bookmarks.value.find(t => t.id === threadId)
    await $fetch(`${BACKEND_URL}/threads/${threadId}/reply`, {
      method: 'POST',
      body: {
        username: user.value.username,
        konten: content,
        replyToUsername: thread?.user?.username
      }
    })
    replyContents.value[threadId] = ''
    if (currentTab.value === 'bookmarks') {
      loadBookmarks()
    } else {
      loadTimeline()
    }
  } catch (err) {
    console.error('Failed to post reply:', err)
    checkUserError(err)
  }
}

const handleDeletePost = async (id) => {
  if (typeof window !== 'undefined' && !window.confirm("Are you sure you want to delete this post?")) return
  try {
    await $fetch(`${BACKEND_URL}/threads/${id}`, {
      method: 'DELETE',
      query: { username: user.value?.username }
    })
    loadTimeline()
    loadProfileThreads()
    if (currentTab.value === 'bookmarks') {
      loadBookmarks()
    }
  } catch (err) {
    console.error('Failed to delete post:', err)
    checkUserError(err)
  }
}

const handleDeleteReply = async (id) => {
  if (typeof window !== 'undefined' && !window.confirm("Are you sure you want to delete this reply?")) return
  try {
    await $fetch(`${BACKEND_URL}/replies/${id}`, {
      method: 'DELETE',
      query: { username: user.value?.username }
    })
    loadTimeline()
    loadProfileThreads()
    if (currentTab.value === 'bookmarks') {
      loadBookmarks()
    }
  } catch (err) {
    console.error('Failed to delete reply:', err)
    checkUserError(err)
  }
}

// News API calls
const loadNews = async () => {
  try {
    newsList.value = await $fetch(`${BACKEND_URL}/news`)
  } catch (err) {
    console.error('Failed to load news:', err)
  }
}

const handleGenerateNews = async () => {
  try {
    await $fetch(`${BACKEND_URL}/news/generate`, { method: 'POST' })
    loadNews()
  } catch (err) {
    console.error('Failed to generate news:', err)
  }
}

// Chatbot API calls
const loadChatbotHistory = async () => {
  if (!user.value) return
  try {
    chatbotMessages.value = await $fetch(`${BACKEND_URL}/chatbot`, {
      query: { username: user.value.username }
    })
  } catch (err) {
    console.error('Failed to load chatbot history:', err)
  }
}

const handleSendChatbotMsg = async () => {
  if (!newChatbotMessage.value.trim()) return
  const msg = newChatbotMessage.value
  newChatbotMessage.value = ''
  
  // Optimistically append user message to local state
  chatbotMessages.value.push({ role: 'user', kontent: msg })
  isChatbotTyping.value = true

  try {
    const res = await $fetch(`${BACKEND_URL}/chatbot`, {
      method: 'POST',
      body: {
        username: user.value.username,
        input: msg
      }
    })
    chatbotMessages.value.push({ role: 'model', kontent: res.response })
  } catch (err) {
    console.error('Chatbot error:', err)
    chatbotMessages.value.push({ role: 'model', kontent: 'Error: Could not connect to Chatbot assistant.' })
  } finally {
    isChatbotTyping.value = false
    loadChatbotHistory() // Reload to sync IDs and correct history
  }
}

const handleClearChatbotHistory = async () => {
  try {
    await $fetch(`${BACKEND_URL}/chatbot`, {
      method: 'DELETE',
      query: { username: user.value.username }
    })
    chatbotMessages.value = []
  } catch (err) {
    console.error('Failed to clear chatbot context:', err)
  }
}

// Profile API calls
const handleUpdateProfile = async () => {
  profileSuccess.value = ''
  profileError.value = ''
  try {
    const res = await $fetch(`${BACKEND_URL}/users/profile`, {
      method: 'PUT',
      body: {
        username: user.value.username,
        fullName: profileFullName.value,
        NomorTelpon: profileNomorTelpon.value,
        email: profileEmail.value,
        password: profilePassword.value || null
      }
    })
    user.value = res
    localStorage.setItem('chirp_user', JSON.stringify(res))
    profileSuccess.value = 'Profile updated successfully!'
    profilePassword.value = ''
  } catch (err) {
    profileError.value = err.data?.error || 'Failed to update profile.'
  }
}

const toggleReplies = (threadId) => {
  expandedThreadReplies.value[threadId] = !expandedThreadReplies.value[threadId]
}

const formatTime = (timeStr) => {
  if (!timeStr) return 'just now'
  const date = new Date(timeStr)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' • ' + date.toLocaleDateString()
}
</script>

<template>
  <div class="min-h-screen bg-amber-50 text-black font-sans selection:bg-black selection:text-amber-300">
    
    <!-- 1. AUTHENTICATION SCREENS (GUEST STATE) -->
    <div v-if="!user" class="min-h-screen flex items-center justify-center p-6 bg-[radial-gradient(#000_1px,transparent_1px)] [background-size:16px_16px]">
      <div class="w-full max-w-md bg-white border-4 border-black p-8 shadow-brutal relative">
        <div class="absolute -top-6 left-6 bg-black text-amber-300 font-bold px-4 py-2 border-2 border-black text-xl">
          [ CHIRP ]
        </div>
        
        <h2 class="text-3xl font-black mb-6 mt-2 tracking-tight">
          {{ authMode === 'login' ? 'WELCOME BACK' : 'CREATE ACCOUNT' }}
        </h2>

        <div v-if="authError" class="bg-red-200 border-2 border-black p-3 mb-4 font-bold text-sm">
          ERROR: {{ authError }}
        </div>

        <!-- Login Form -->
        <form v-if="authMode === 'login'" @submit.prevent="handleLogin" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm">USERNAME</label>
            <input type="text" v-model="loginUsername" required class="border-2 border-black p-2 outline-none font-bold shadow-brutal-sm focus:translate-y-[-1px] transition-all bg-amber-50" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="font-bold text-sm">PASSWORD</label>
            <input type="password" v-model="loginPassword" required class="border-2 border-black p-2 outline-none font-bold shadow-brutal-sm focus:translate-y-[-1px] transition-all bg-amber-50" />
          </div>

          <button type="submit" class="bg-amber-300 border-2 border-black py-2 mt-4 font-black shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all">
            LOGIN
          </button>
        </form>

        <!-- Register Form -->
        <form v-else @submit.prevent="handleRegister" class="flex flex-col gap-3">
          <div class="flex flex-col gap-1">
            <label class="font-bold text-xs">USERNAME</label>
            <input type="text" v-model="registerUsername" required class="border-2 border-black p-1.5 outline-none font-bold bg-amber-50 text-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="font-bold text-xs">FULL NAME</label>
            <input type="text" v-model="registerFullName" required class="border-2 border-black p-1.5 outline-none font-bold bg-amber-50 text-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="font-bold text-xs">EMAIL</label>
            <input type="email" v-model="registerEmail" required class="border-2 border-black p-1.5 outline-none font-bold bg-amber-50 text-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="font-bold text-xs">PHONE NUMBER</label>
            <input type="text" v-model="registerNomorTelpon" required class="border-2 border-black p-1.5 outline-none font-bold bg-amber-50 text-sm" />
          </div>

          <div class="flex flex-col gap-1">
            <label class="font-bold text-xs">PASSWORD</label>
            <input type="password" v-model="registerPassword" required class="border-2 border-black p-1.5 outline-none font-bold bg-amber-50 text-sm" />
          </div>

          <button type="submit" class="bg-amber-300 border-2 border-black py-2 mt-3 font-black shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all">
            REGISTER
          </button>
        </form>

        <!-- Toggle Auth Mode -->
        <div class="mt-6 text-center border-t-2 border-black pt-4">
          <button @click="authMode = authMode === 'login' ? 'register' : 'login'; authError = ''" class="font-bold hover:underline text-sm text-gray-700">
            {{ authMode === 'login' ? 'Need an account? Register here' : 'Already have an account? Login here' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 2. APPLICATION DASHBOARD (LOGGED IN STATE) -->
    <div v-else class="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-4 min-h-screen border-x-4 border-black bg-white shadow-brutal-lg">
      
      <!-- LEFT SIDEBAR: NAVIGATION -->
      <aside class="col-span-1 border-r-4 border-black p-6 flex flex-col justify-between sticky top-0 h-screen bg-white">
        <div>
          <div class="bg-black text-amber-300 text-3xl font-black p-4 inline-block mb-10 border-2 border-black shadow-brutal">
            [ CHIRP ]
          </div>

          <div class="bg-amber-100 border-2 border-black p-3 mb-6 font-bold text-sm shadow-brutal-sm">
            Logged in as:<br>
            <span class="text-lg font-black text-black">@{{ user.username }}</span>
          </div>

          <nav class="flex flex-col gap-4 font-black text-lg">
            <button @click="currentTab = 'feed'; loadTimeline()" :class="['flex items-center gap-3 p-2 border-2 border-transparent text-left hover:border-black hover:bg-amber-50 transition-all', currentTab === 'feed' ? 'bg-amber-200 border-black border-2 shadow-brutal-sm' : '']">
              <span class="border-2 border-black px-2 py-0.5 text-xs bg-white shadow-brutal-sm">H</span>
              Home Feed
            </button>
            <button @click="currentTab = 'bookmarks'; loadBookmarks()" :class="['flex items-center gap-3 p-2 border-2 border-transparent text-left hover:border-black hover:bg-amber-50 transition-all', currentTab === 'bookmarks' ? 'bg-amber-200 border-black border-2 shadow-brutal-sm' : '']">
              <span class="border-2 border-black px-2 py-0.5 text-xs bg-white shadow-brutal-sm">B</span>
              Bookmarks
            </button>
            <button @click="currentTab = 'messages'; loadChatbotHistory()" :class="['flex items-center gap-3 p-2 border-2 border-transparent text-left hover:border-black hover:bg-amber-50 transition-all', currentTab === 'messages' ? 'bg-amber-200 border-black border-2 shadow-brutal-sm' : '']">
              <span class="border-2 border-black px-2 py-0.5 text-xs bg-white shadow-brutal-sm">M</span>
              AI Chatbot DM
            </button>
            <button @click="currentTab = 'profile'; selectedProfileUser = user; initializeProfileFields(); loadLocalInteractions(); profileTab = 'posts'; loadProfileThreads()" :class="['flex items-center gap-3 p-2 border-2 border-transparent text-left hover:border-black hover:bg-amber-50 transition-all', currentTab === 'profile' ? 'bg-amber-200 border-black border-2 shadow-brutal-sm' : '']">
              <span class="border-2 border-black px-2 py-0.5 text-xs bg-white shadow-brutal-sm">P</span>
              My Profile
            </button>
          </nav>
        </div>

        <div class="flex flex-col gap-3">
          <button @click="handleLogout" class="bg-red-200 border-2 border-black py-2 font-black shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all text-sm">
            LOGOUT
          </button>
        </div>
      </aside>

      <!-- MIDDLE COLUMN: CONTENT PANELS -->
      <main class="col-span-2 border-r-4 border-black bg-white">
        
        <!-- A. HOME FEED TAB -->
        <div v-if="currentTab === 'feed'">
          <header class="border-b-4 border-black p-4 font-black text-xl flex justify-between items-center sticky top-0 bg-white z-10">
            <span>[ HOME TIMELINE ]</span>
            <div class="flex border-2 border-black bg-amber-50 shadow-brutal-sm">
              <button @click="activeFilter = 'foryou'" :class="['px-3 py-1 font-bold text-xs border-r-2 border-black', activeFilter === 'foryou' ? 'bg-amber-300' : '']">For You</button>
              <button @click="activeFilter = 'trending'" :class="['px-3 py-1 font-bold text-xs', activeFilter === 'trending' ? 'bg-amber-300' : '']">Trending</button>
            </div>
          </header>

          <div class="p-6">
            <!-- Posting Box -->
            <div class="border-4 border-black p-4 bg-amber-50 shadow-brutal mb-8">
              <h3 class="font-black text-sm mb-2">[ CREATE A THREAD ]</h3>
              <textarea v-model="newPostContent" placeholder="What is happening?! (Supports #hashtags)" class="w-full h-24 border-2 border-black p-2 font-bold outline-none resize-none bg-white focus:shadow-brutal-sm transition-all" maxLength="280"></textarea>
              <div class="flex justify-between items-center mt-3">
                <span class="text-xs font-bold text-gray-500">{{ newPostContent.length }}/280 chars</span>
                <button @click="handleCreatePost" class="bg-amber-300 border-2 border-black px-6 py-1.5 font-black text-sm shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all">
                  POST
                </button>
              </div>
            </div>

            <!-- Feed Post List -->
            <div class="flex flex-col gap-6">
              <div v-if="threads.length === 0" class="border-4 border-black p-8 shadow-brutal bg-amber-100 text-center">
                <p class="font-black text-lg">No threads found.</p>
                <p class="font-bold text-gray-600 text-sm mt-1">Be the first to post something above or change the search query!</p>
              </div>

              <!-- Thread Card Component -->
              <div v-for="t in threads" :key="t.id" class="border-4 border-black bg-white p-5 shadow-brutal relative">
                <!-- User Info -->
                <div class="flex justify-between items-start mb-2 border-b-2 border-black pb-2">
                  <div>
                    <span @click="viewUserProfile(t.user?.username)" class="font-black text-lg text-black hover:underline cursor-pointer">@{{ t.user?.username }}</span>
                    <span class="text-xs font-bold text-gray-500 bg-amber-100 border border-black px-1.5 py-0.5 ml-2">{{ t.user?.fullName }}</span>
                  </div>
                  <span class="text-xs font-bold text-gray-500">{{ formatTime(t.tanggalPublic) }}</span>
                </div>

                <!-- Content -->
                <p class="font-bold text-md text-gray-800 my-4 leading-snug whitespace-pre-wrap">{{ t.konten }}</p>

                <!-- Actions -->
                <div class="flex gap-2 border-t-2 border-black pt-3 mt-4 text-xs font-black">
                  <button @click="handleLike(t.id)" class="border-2 border-black bg-red-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ♥ LIKE ({{ t.like }})
                  </button>
                  <button @click="handleRepost(t.id)" class="border-2 border-black bg-green-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ⇅ REPOST ({{ t.repost }})
                  </button>
                  <button @click="handleBookmark(t.id)" class="border-2 border-black bg-blue-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    🔖 BOOKMARK ({{ t.bookmark }})
                  </button>
                  <button v-if="t.user?.username === user?.username" @click="handleDeletePost(t.id)" class="border-2 border-black bg-gray-100 px-3 py-1 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all flex items-center gap-1.5">
                    🗑️ DELETE
                  </button>
                  <button @click="toggleReplies(t.id)" class="border-2 border-black bg-amber-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all ml-auto">
                    💬 REPLIES ({{ t.reply ? t.reply.length : 0 }})
                  </button>
                </div>

                <!-- Nested Replies Panel -->
                <div v-if="expandedThreadReplies[t.id]" class="border-t-4 border-black mt-4 pt-4 bg-amber-50 -mx-5 -mb-5 p-5">
                  <h4 class="font-black text-xs mb-3 text-gray-700">[ CONVERSATION THREAD ]</h4>
                  
                  <!-- Replies List -->
                  <div class="flex flex-col gap-3 max-h-60 overflow-y-auto mb-4">
                    <div v-if="!t.reply || t.reply.length === 0" class="font-bold text-sm text-gray-500 text-center italic py-2">
                      No replies yet. Start the conversation below!
                    </div>
                    <div v-for="rep in t.reply" :key="rep.id" class="border-2 border-black bg-white p-3 shadow-brutal-sm text-sm">
                      <div class="flex justify-between items-center border-b border-black pb-1 mb-1 font-black">
                        <div class="flex items-center gap-1.5 flex-wrap">
                          <span @click="viewUserProfile(rep.user?.username)" class="cursor-pointer hover:underline">@{{ rep.user?.username }}</span>
                          <span v-if="rep.replyTo" class="text-xs text-gray-500">
                            replying to <span @click="viewUserProfile(rep.replyTo?.username)" class="cursor-pointer hover:underline font-bold">@{{ rep.replyTo?.username }}</span>
                          </span>
                        </div>
                        <button v-if="rep.user?.username === user?.username" @click="handleDeleteReply(rep.id)" class="border-2 border-black bg-gray-50 px-1.5 py-0.5 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all text-[10px] font-black">
                          🗑️
                        </button>
                      </div>
                      <p class="font-medium text-gray-800">{{ rep.konten }}</p>
                    </div>
                  </div>

                  <!-- Reply Box -->
                  <div class="flex gap-2">
                    <input type="text" v-model="replyContents[t.id]" placeholder="Reply to this thread..." class="flex-grow border-2 border-black p-2 text-sm font-bold outline-none bg-white focus:border-amber-400" />
                    <button @click="handlePostReply(t.id)" class="bg-amber-300 border-2 border-black px-4 font-black text-xs shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all">
                      REPLY
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- D. BOOKMARKS TAB -->
        <div v-if="currentTab === 'bookmarks'">
          <header class="border-b-4 border-black p-4 font-black text-xl bg-white sticky top-0 z-10 flex justify-between items-center">
            <span>[ BOOKMARKED THREADS ]</span>
          </header>

          <div class="p-6">
            <!-- Bookmarked Post List -->
            <div class="flex flex-col gap-6">
              <div v-if="bookmarks.length === 0" class="border-4 border-black p-8 shadow-brutal bg-amber-100 text-center">
                <p class="font-black text-lg">No bookmarked threads yet.</p>
                <p class="font-bold text-gray-600 text-sm mt-1">Bookmark posts in the home feed to save them here!</p>
              </div>

              <!-- Thread Card Component for Bookmarks -->
              <div v-for="t in bookmarks" :key="t.id" class="border-4 border-black bg-white p-5 shadow-brutal relative">
                <!-- User Info -->
                <div class="flex justify-between items-start mb-2 border-b-2 border-black pb-2">
                  <div>
                    <span @click="viewUserProfile(t.user?.username)" class="font-black text-lg text-black hover:underline cursor-pointer">@{{ t.user?.username }}</span>
                    <span class="text-xs font-bold text-gray-500 bg-amber-100 border border-black px-1.5 py-0.5 ml-2">{{ t.user?.fullName }}</span>
                  </div>
                  <span class="text-xs font-bold text-gray-500">{{ formatTime(t.tanggalPublic) }}</span>
                </div>

                <!-- Content -->
                <p class="font-bold text-md text-gray-800 my-4 leading-snug whitespace-pre-wrap">{{ t.konten }}</p>

                <!-- Actions -->
                <div class="flex gap-2 border-t-2 border-black pt-3 mt-4 text-xs font-black">
                  <button @click="handleLike(t.id)" class="border-2 border-black bg-red-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ♥ LIKE ({{ t.like }})
                  </button>
                  <button @click="handleRepost(t.id)" class="border-2 border-black bg-green-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ⇅ REPOST ({{ t.repost }})
                  </button>
                  <button @click="handleBookmark(t.id)" class="border-2 border-black bg-blue-300 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5 font-bold">
                    🔖 BOOKMARKED ({{ t.bookmark }})
                  </button>
                  <button v-if="t.user?.username === user?.username" @click="handleDeletePost(t.id)" class="border-2 border-black bg-gray-100 px-3 py-1 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all flex items-center gap-1.5">
                    🗑️ DELETE
                  </button>
                  <button @click="toggleReplies(t.id)" class="border-2 border-black bg-amber-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all ml-auto">
                    💬 REPLIES ({{ t.reply ? t.reply.length : 0 }})
                  </button>
                </div>

                <!-- Nested Replies Panel -->
                <div v-if="expandedThreadReplies[t.id]" class="border-t-4 border-black mt-4 pt-4 bg-amber-50 -mx-5 -mb-5 p-5">
                  <h4 class="font-black text-xs mb-3 text-gray-700">[ CONVERSATION THREAD ]</h4>
                  
                  <!-- Replies List -->
                  <div class="flex flex-col gap-3 max-h-60 overflow-y-auto mb-4">
                    <div v-if="!t.reply || t.reply.length === 0" class="font-bold text-sm text-gray-500 text-center italic py-2">
                      No replies yet. Start the conversation below!
                    </div>
                    <div v-for="rep in t.reply" :key="rep.id" class="border-2 border-black bg-white p-3 shadow-brutal-sm text-sm">
                      <div class="flex justify-between items-center border-b border-black pb-1 mb-1 font-black">
                        <div class="flex items-center gap-1.5 flex-wrap">
                          <span @click="viewUserProfile(rep.user?.username)" class="cursor-pointer hover:underline">@{{ rep.user?.username }}</span>
                          <span v-if="rep.replyTo" class="text-xs text-gray-500">
                            replying to <span @click="viewUserProfile(rep.replyTo?.username)" class="cursor-pointer hover:underline font-bold">@{{ rep.replyTo?.username }}</span>
                          </span>
                        </div>
                        <button v-if="rep.user?.username === user?.username" @click="handleDeleteReply(rep.id)" class="border-2 border-black bg-gray-50 px-1.5 py-0.5 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all text-[10px] font-black">
                          🗑️
                        </button>
                      </div>
                      <p class="font-medium text-gray-800">{{ rep.konten }}</p>
                    </div>
                  </div>

                  <!-- Reply Box -->
                  <div class="flex gap-2">
                    <input type="text" v-model="replyContents[t.id]" placeholder="Reply to this thread..." class="flex-grow border-2 border-black p-2 text-sm font-bold outline-none bg-white focus:border-amber-400" />
                    <button @click="handlePostReply(t.id)" class="bg-amber-300 border-2 border-black px-4 font-black text-xs shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all">
                      REPLY
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- B. AI CHATBOT TAB -->
        <div v-if="currentTab === 'messages'" class="flex flex-col h-screen">
          <header class="border-b-4 border-black p-4 font-black text-xl flex justify-between items-center bg-white z-10 shrink-0">
            <span>[ CHIRPY ASSISTANT ]</span>
            <button @click="handleClearChatbotHistory" class="border-2 border-black bg-red-100 px-3 py-1 text-xs font-black shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] transition-all">
              CLEAR CONTEXT
            </button>
          </header>

          <!-- Messages Chat History -->
          <div class="flex-grow overflow-y-auto p-6 flex flex-col gap-4 bg-[linear-gradient(#f0f0f0_1px,transparent_1px),linear-gradient(90deg,#f0f0f0_1px,transparent_1px)] bg-[size:20px_20px] bg-amber-50">
            <div class="border-2 border-black bg-white p-4 font-bold text-sm shadow-brutal-sm leading-relaxed">
              🤖 <span class="font-black text-amber-500">CHIRPY:</span> Hello @{{ user.username }}! I am your AI assistant running on OpenRouter. Ask me to draft tweets, write stories, explain concepts, or summarize news. I'm here to chirp with you!
            </div>

            <div v-for="msg in chatbotMessages" :key="msg.id" :class="['border-2 border-black p-4 max-w-[85%] font-bold text-sm shadow-brutal-sm leading-relaxed', msg.role === 'user' ? 'bg-amber-200 self-end' : 'bg-white self-start']">
              <span class="font-black block mb-1" :class="msg.role === 'user' ? 'text-black' : 'text-amber-500'">
                {{ msg.role === 'user' ? 'YOU' : 'CHIRPY' }}
              </span>
              <p class="font-medium text-gray-800">{{ msg.kontent || msg.message }}</p>
            </div>

            <!-- Typing Indicator -->
            <div v-if="isChatbotTyping" class="border-2 border-black bg-white p-3 self-start font-black text-xs animate-pulse shadow-brutal-sm">
              [ CHIRPY IS TYPING... ]
            </div>
          </div>

          <!-- Message Input Bar -->
          <div class="border-t-4 border-black p-4 bg-white shrink-0">
            <form @submit.prevent="handleSendChatbotMsg" class="flex gap-3">
              <input type="text" v-model="newChatbotMessage" placeholder="Ask Chirpy assistant... (e.g. 'Draft a funny post about OOP')" required class="flex-grow border-2 border-black p-3 font-bold outline-none bg-amber-50 focus:translate-y-[-1px] transition-all" />
              <button type="submit" class="bg-amber-300 border-2 border-black px-6 font-black shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all">
                SEND
              </button>
            </form>
          </div>
        </div>

        <!-- C. PROFILE EDIT TAB -->
        <div v-if="currentTab === 'profile'">
          <header class="border-b-4 border-black p-4 font-black text-xl bg-white sticky top-0 z-10">
            <span>[ USER PROFILE ]</span>
          </header>

          <div class="p-6" v-if="selectedProfileUser">
            <!-- Profile Info Banner -->
            <div class="border-4 border-black bg-amber-100 p-6 shadow-brutal mb-6 relative">
              <div class="flex items-center gap-4">
                <!-- Avatar circle with user initial -->
                <div class="w-16 h-16 bg-amber-300 border-4 border-black flex items-center justify-center font-black text-2xl shadow-brutal-sm">
                  {{ selectedProfileUser.fullName ? selectedProfileUser.fullName.charAt(0).toUpperCase() : selectedProfileUser.username.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <h2 class="text-2xl font-black">{{ selectedProfileUser.fullName }}</h2>
                  <p class="text-sm font-bold text-gray-700">@{{ selectedProfileUser.username }}</p>
                </div>
              </div>
              <div class="mt-4 flex flex-wrap gap-4 text-xs font-bold">
                <span class="bg-white border border-black px-2 py-1 shadow-brutal-sm">📧 {{ selectedProfileUser.email }}</span>
                <span v-if="selectedProfileUser.nomorTelpon" class="bg-white border border-black px-2 py-1 shadow-brutal-sm">📞 {{ selectedProfileUser.nomorTelpon }}</span>
              </div>
            </div>

            <!-- Profile Sub-navigation -->
            <div class="flex border-4 border-black bg-white shadow-brutal-sm mb-6">
              <button @click="profileTab = 'posts'" :class="['flex-1 py-2 font-black text-xs text-center border-r-2 border-black hover:bg-amber-50 transition-all', profileTab === 'posts' ? 'bg-amber-300' : '']">
                📝 POSTS ({{ myTweets.length }})
              </button>
              <button @click="profileTab = 'likes'" :class="['flex-1 py-2 font-black text-xs text-center border-r-2 border-black hover:bg-amber-50 transition-all', profileTab === 'likes' ? 'bg-amber-300' : '']">
                ❤️ LIKES ({{ myLikedTweets.length }})
              </button>
              <button @click="profileTab = 'reposts'" :class="['flex-1 py-2 font-black text-xs text-center hover:bg-amber-50 transition-all', profileTab === 'reposts' ? 'bg-amber-300' : '', selectedProfileUser?.username === user?.username ? 'border-r-2 border-black' : '']">
                🔁 REPOSTS ({{ myRepostedTweets.length }})
              </button>
              <button v-if="selectedProfileUser?.username === user?.username" @click="profileTab = 'edit'" :class="['flex-1 py-2 font-black text-xs text-center hover:bg-amber-50 transition-all', profileTab === 'edit' ? 'bg-amber-300' : '']">
                ⚙️ SETTINGS
              </button>
            </div>

            <!-- TAB 1: USER POSTS -->
            <div v-if="profileTab === 'posts'" class="flex flex-col gap-6">
              <div v-if="myTweets.length === 0" class="border-4 border-black p-8 shadow-brutal bg-amber-100 text-center">
                <p class="font-black text-lg">
                  {{ selectedProfileUser?.username === user?.username ? "You haven't posted any threads yet." : '@' + selectedProfileUser?.username + " hasn't posted any threads yet." }}
                </p>
                <p v-if="selectedProfileUser?.username === user?.username" class="font-bold text-gray-600 text-sm mt-1">Go back to the home feed to write your first post!</p>
              </div>

              <!-- Thread Card Component -->
              <div v-for="t in myTweets" :key="t.id" class="border-4 border-black bg-white p-5 shadow-brutal relative">
                <!-- User Info -->
                <div class="flex justify-between items-start mb-2 border-b-2 border-black pb-2">
                  <div>
                    <span @click="viewUserProfile(t.user?.username)" class="font-black text-lg text-black hover:underline cursor-pointer">@{{ t.user?.username }}</span>
                    <span class="text-xs font-bold text-gray-500 bg-amber-100 border border-black px-1.5 py-0.5 ml-2">{{ t.user?.fullName }}</span>
                  </div>
                  <span class="text-xs font-bold text-gray-500">{{ formatTime(t.tanggalPublic) }}</span>
                </div>

                <!-- Content -->
                <p class="font-bold text-md text-gray-800 my-4 leading-snug whitespace-pre-wrap">{{ t.konten }}</p>

                <!-- Actions -->
                <div class="flex gap-2 border-t-2 border-black pt-3 mt-4 text-xs font-black">
                  <button @click="handleLike(t.id)" class="border-2 border-black bg-red-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ♥ LIKE ({{ t.like }})
                  </button>
                  <button @click="handleRepost(t.id)" class="border-2 border-black bg-green-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ⇅ REPOST ({{ t.repost }})
                  </button>
                  <button @click="handleBookmark(t.id)" :class="['border-2 border-black px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5', bookmarks.some(b => b.id === t.id) ? 'bg-blue-300' : 'bg-blue-100']">
                    🔖 {{ bookmarks.some(b => b.id === t.id) ? 'BOOKMARKED' : 'BOOKMARK' }} ({{ t.bookmark }})
                  </button>
                  <button v-if="t.user?.username === user?.username" @click="handleDeletePost(t.id)" class="border-2 border-black bg-gray-100 px-3 py-1 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all flex items-center gap-1.5">
                    🗑️ DELETE
                  </button>
                  <button @click="toggleReplies(t.id)" class="border-2 border-black bg-amber-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all ml-auto">
                    💬 REPLIES ({{ t.reply ? t.reply.length : 0 }})
                  </button>
                </div>

                <!-- Nested Replies Panel -->
                <div v-if="expandedThreadReplies[t.id]" class="border-t-4 border-black mt-4 pt-4 bg-amber-50 -mx-5 -mb-5 p-5">
                  <h4 class="font-black text-xs mb-3 text-gray-700">[ CONVERSATION THREAD ]</h4>
                  
                  <!-- Replies List -->
                  <div class="flex flex-col gap-3 max-h-60 overflow-y-auto mb-4">
                    <div v-if="!t.reply || t.reply.length === 0" class="font-bold text-sm text-gray-500 text-center italic py-2">
                      No replies yet. Start the conversation below!
                    </div>
                    <div v-for="rep in t.reply" :key="rep.id" class="border-2 border-black bg-white p-3 shadow-brutal-sm text-sm">
                      <div class="flex justify-between items-center border-b border-black pb-1 mb-1 font-black">
                        <div class="flex items-center gap-1.5 flex-wrap">
                          <span @click="viewUserProfile(rep.user?.username)" class="cursor-pointer hover:underline">@{{ rep.user?.username }}</span>
                          <span v-if="rep.replyTo" class="text-xs text-gray-500">
                            replying to <span @click="viewUserProfile(rep.replyTo?.username)" class="cursor-pointer hover:underline font-bold">@{{ rep.replyTo?.username }}</span>
                          </span>
                        </div>
                        <button v-if="rep.user?.username === user?.username" @click="handleDeleteReply(rep.id)" class="border-2 border-black bg-gray-50 px-1.5 py-0.5 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all text-[10px] font-black">
                          🗑️
                        </button>
                      </div>
                      <p class="font-medium text-gray-800">{{ rep.konten }}</p>
                    </div>
                  </div>

                  <!-- Reply Box -->
                  <div class="flex gap-2">
                    <input type="text" v-model="replyContents[t.id]" placeholder="Reply to this thread..." class="flex-grow border-2 border-black p-2 text-sm font-bold outline-none bg-white focus:border-amber-400" />
                    <button @click="handlePostReply(t.id)" class="bg-amber-300 border-2 border-black px-4 font-black text-xs shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all">
                      REPLY
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- TAB 2: USER LIKES -->
            <div v-if="profileTab === 'likes'" class="flex flex-col gap-6">
              <div v-if="myLikedTweets.length === 0" class="border-4 border-black p-8 shadow-brutal bg-amber-100 text-center">
                <p class="font-black text-lg">
                  {{ selectedProfileUser?.username === user?.username ? "You haven't liked any threads yet." : '@' + selectedProfileUser?.username + " hasn't liked any threads yet." }}
                </p>
                <p v-if="selectedProfileUser?.username === user?.username" class="font-bold text-gray-600 text-sm mt-1">Like posts in the feed to see them saved here!</p>
              </div>

              <!-- Thread Card Component -->
              <div v-for="t in myLikedTweets" :key="t.id" class="border-4 border-black bg-white p-5 shadow-brutal relative">
                <!-- User Liked Header -->
                <div class="text-xs font-black text-red-500 mb-2 flex items-center gap-1 border-b border-dashed border-red-200 pb-1.5">
                  <span>❤️</span> {{ selectedProfileUser.fullName }} liked this post
                </div>

                <!-- User Info -->
                <div class="flex justify-between items-start mb-2 border-b-2 border-black pb-2">
                  <div>
                    <span @click="viewUserProfile(t.user?.username)" class="font-black text-lg text-black hover:underline cursor-pointer">@{{ t.user?.username }}</span>
                    <span class="text-xs font-bold text-gray-500 bg-amber-100 border border-black px-1.5 py-0.5 ml-2">{{ t.user?.fullName }}</span>
                  </div>
                  <span class="text-xs font-bold text-gray-500">{{ formatTime(t.tanggalPublic) }}</span>
                </div>

                <!-- Content -->
                <p class="font-bold text-md text-gray-800 my-4 leading-snug whitespace-pre-wrap">{{ t.konten }}</p>

                <!-- Actions -->
                <div class="flex gap-2 border-t-2 border-black pt-3 mt-4 text-xs font-black">
                  <button @click="handleLike(t.id)" class="border-2 border-black bg-red-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ♥ LIKE ({{ t.like }})
                  </button>
                  <button @click="handleRepost(t.id)" class="border-2 border-black bg-green-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ⇅ REPOST ({{ t.repost }})
                  </button>
                  <button @click="handleBookmark(t.id)" :class="['border-2 border-black px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5', bookmarks.some(b => b.id === t.id) ? 'bg-blue-300' : 'bg-blue-100']">
                    🔖 {{ bookmarks.some(b => b.id === t.id) ? 'BOOKMARKED' : 'BOOKMARK' }} ({{ t.bookmark }})
                  </button>
                  <button v-if="t.user?.username === user?.username" @click="handleDeletePost(t.id)" class="border-2 border-black bg-gray-100 px-3 py-1 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all flex items-center gap-1.5">
                    🗑️ DELETE
                  </button>
                  <button @click="toggleReplies(t.id)" class="border-2 border-black bg-amber-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all ml-auto">
                    💬 REPLIES ({{ t.reply ? t.reply.length : 0 }})
                  </button>
                </div>

                <!-- Nested Replies Panel -->
                <div v-if="expandedThreadReplies[t.id]" class="border-t-4 border-black mt-4 pt-4 bg-amber-50 -mx-5 -mb-5 p-5">
                  <h4 class="font-black text-xs mb-3 text-gray-700">[ CONVERSATION THREAD ]</h4>
                  
                  <!-- Replies List -->
                  <div class="flex flex-col gap-3 max-h-60 overflow-y-auto mb-4">
                    <div v-if="!t.reply || t.reply.length === 0" class="font-bold text-sm text-gray-500 text-center italic py-2">
                      No replies yet. Start the conversation below!
                    </div>
                    <div v-for="rep in t.reply" :key="rep.id" class="border-2 border-black bg-white p-3 shadow-brutal-sm text-sm">
                      <div class="flex justify-between items-center border-b border-black pb-1 mb-1 font-black">
                        <div class="flex items-center gap-1.5 flex-wrap">
                          <span @click="viewUserProfile(rep.user?.username)" class="cursor-pointer hover:underline">@{{ rep.user?.username }}</span>
                          <span v-if="rep.replyTo" class="text-xs text-gray-500">
                            replying to <span @click="viewUserProfile(rep.replyTo?.username)" class="cursor-pointer hover:underline font-bold">@{{ rep.replyTo?.username }}</span>
                          </span>
                        </div>
                        <button v-if="rep.user?.username === user?.username" @click="handleDeleteReply(rep.id)" class="border-2 border-black bg-gray-50 px-1.5 py-0.5 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all text-[10px] font-black">
                          🗑️
                        </button>
                      </div>
                      <p class="font-medium text-gray-800">{{ rep.konten }}</p>
                    </div>
                  </div>

                  <!-- Reply Box -->
                  <div class="flex gap-2">
                    <input type="text" v-model="replyContents[t.id]" placeholder="Reply to this thread..." class="flex-grow border-2 border-black p-2 text-sm font-bold outline-none bg-white focus:border-amber-400" />
                    <button @click="handlePostReply(t.id)" class="bg-amber-300 border-2 border-black px-4 font-black text-xs shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all">
                      REPLY
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- TAB 3: USER REPOSTS -->
            <div v-if="profileTab === 'reposts'" class="flex flex-col gap-6">
              <div v-if="myRepostedTweets.length === 0" class="border-4 border-black p-8 shadow-brutal bg-amber-100 text-center">
                <p class="font-black text-lg">
                  {{ selectedProfileUser?.username === user?.username ? "You haven't reposted any threads yet." : '@' + selectedProfileUser?.username + " hasn't reposted any threads yet." }}
                </p>
                <p v-if="selectedProfileUser?.username === user?.username" class="font-bold text-gray-600 text-sm mt-1">Repost threads in the feed to see them saved here!</p>
              </div>

              <!-- Thread Card Component -->
              <div v-for="t in myRepostedTweets" :key="t.id" class="border-4 border-black bg-white p-5 shadow-brutal relative">
                <!-- User Reposted Header -->
                <div class="text-xs font-black text-green-600 mb-2 flex items-center gap-1 border-b border-dashed border-green-200 pb-1.5">
                  <span>🔁</span> {{ selectedProfileUser.fullName }} reposted
                </div>

                <!-- User Info -->
                <div class="flex justify-between items-start mb-2 border-b-2 border-black pb-2">
                  <div>
                    <span @click="viewUserProfile(t.user?.username)" class="font-black text-lg text-black hover:underline cursor-pointer">@{{ t.user?.username }}</span>
                    <span class="text-xs font-bold text-gray-500 bg-amber-100 border border-black px-1.5 py-0.5 ml-2">{{ t.user?.fullName }}</span>
                  </div>
                  <span class="text-xs font-bold text-gray-500">{{ formatTime(t.tanggalPublic) }}</span>
                </div>

                <!-- Content -->
                <p class="font-bold text-md text-gray-800 my-4 leading-snug whitespace-pre-wrap">{{ t.konten }}</p>

                <!-- Actions -->
                <div class="flex gap-2 border-t-2 border-black pt-3 mt-4 text-xs font-black">
                  <button @click="handleLike(t.id)" class="border-2 border-black bg-red-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ♥ LIKE ({{ t.like }})
                  </button>
                  <button @click="handleRepost(t.id)" class="border-2 border-black bg-green-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5">
                    ⇅ REPOST ({{ t.repost }})
                  </button>
                  <button @click="handleBookmark(t.id)" :class="['border-2 border-black px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all flex items-center gap-1.5', bookmarks.some(b => b.id === t.id) ? 'bg-blue-300' : 'bg-blue-100']">
                    🔖 {{ bookmarks.some(b => b.id === t.id) ? 'BOOKMARKED' : 'BOOKMARK' }} ({{ t.bookmark }})
                  </button>
                  <button v-if="t.user?.username === user?.username" @click="handleDeletePost(t.id)" class="border-2 border-black bg-gray-100 px-3 py-1 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all flex items-center gap-1.5">
                    🗑️ DELETE
                  </button>
                  <button @click="toggleReplies(t.id)" class="border-2 border-black bg-amber-100 px-3 py-1 shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all ml-auto">
                    💬 REPLIES ({{ t.reply ? t.reply.length : 0 }})
                  </button>
                </div>

                <!-- Nested Replies Panel -->
                <div v-if="expandedThreadReplies[t.id]" class="border-t-4 border-black mt-4 pt-4 bg-amber-50 -mx-5 -mb-5 p-5">
                  <h4 class="font-black text-xs mb-3 text-gray-700">[ CONVERSATION THREAD ]</h4>
                  
                  <!-- Replies List -->
                  <div class="flex flex-col gap-3 max-h-60 overflow-y-auto mb-4">
                    <div v-if="!t.reply || t.reply.length === 0" class="font-bold text-sm text-gray-500 text-center italic py-2">
                      No replies yet. Start the conversation below!
                    </div>
                    <div v-for="rep in t.reply" :key="rep.id" class="border-2 border-black bg-white p-3 shadow-brutal-sm text-sm">
                      <div class="flex justify-between items-center border-b border-black pb-1 mb-1 font-black">
                        <div class="flex items-center gap-1.5 flex-wrap">
                          <span @click="viewUserProfile(rep.user?.username)" class="cursor-pointer hover:underline">@{{ rep.user?.username }}</span>
                          <span v-if="rep.replyTo" class="text-xs text-gray-500">
                            replying to <span @click="viewUserProfile(rep.replyTo?.username)" class="cursor-pointer hover:underline font-bold">@{{ rep.replyTo?.username }}</span>
                          </span>
                        </div>
                        <button v-if="rep.user?.username === user?.username" @click="handleDeleteReply(rep.id)" class="border-2 border-black bg-gray-50 px-1.5 py-0.5 shadow-brutal-sm hover:bg-red-400 hover:text-white transition-all text-[10px] font-black">
                          🗑️
                        </button>
                      </div>
                      <p class="font-medium text-gray-800">{{ rep.konten }}</p>
                    </div>
                  </div>

                  <!-- Reply Box -->
                  <div class="flex gap-2">
                    <input type="text" v-model="replyContents[t.id]" placeholder="Reply to this thread..." class="flex-grow border-2 border-black p-2 text-sm font-bold outline-none bg-white focus:border-amber-400" />
                    <button @click="handlePostReply(t.id)" class="bg-amber-300 border-2 border-black px-4 font-black text-xs shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all">
                      REPLY
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- TAB 4: EDIT SETTINGS -->
            <div v-if="profileTab === 'edit' && selectedProfileUser?.username === user?.username" class="max-w-lg">
              <h3 class="text-2xl font-black mb-6">[ EDIT ACCOUNT DETAILS ]</h3>

              <div v-if="profileSuccess" class="bg-green-200 border-2 border-black p-3 mb-4 font-bold text-sm shadow-brutal-sm">
                SUCCESS: {{ profileSuccess }}
              </div>
              <div v-if="profileError" class="bg-red-200 border-2 border-black p-3 mb-4 font-bold text-sm shadow-brutal-sm">
                ERROR: {{ profileError }}
              </div>

              <form @submit.prevent="handleUpdateProfile" class="flex flex-col gap-4">
                <div class="flex flex-col gap-1">
                  <label class="font-bold text-sm text-gray-700">USERNAME (UNALTERABLE)</label>
                  <input type="text" :value="user.username" disabled class="border-2 border-black p-2 font-bold bg-gray-100 outline-none text-gray-500 cursor-not-allowed" />
                </div>

                <div class="flex flex-col gap-1">
                  <label class="font-bold text-sm">FULL NAME</label>
                  <input type="text" v-model="profileFullName" required class="border-2 border-black p-2 font-bold bg-amber-50 outline-none focus:bg-white" />
                </div>

                <div class="flex flex-col gap-1">
                  <label class="font-bold text-sm">EMAIL ADDRESS</label>
                  <input type="email" v-model="profileEmail" required class="border-2 border-black p-2 font-bold bg-amber-50 outline-none focus:bg-white" />
                </div>

                <div class="flex flex-col gap-1">
                  <label class="font-bold text-sm">PHONE NUMBER</label>
                  <input type="text" v-model="profileNomorTelpon" required class="border-2 border-black p-2 font-bold bg-amber-50 outline-none focus:bg-white" />
                </div>

                <div class="flex flex-col gap-1">
                  <label class="font-bold text-sm">NEW PASSWORD (LEAVE EMPTY TO KEEP CURRENT)</label>
                  <input type="password" v-model="profilePassword" placeholder="••••••••" class="border-2 border-black p-2 font-bold bg-amber-50 outline-none focus:bg-white" />
                </div>

                <button type="submit" class="bg-amber-300 border-2 border-black py-2.5 mt-4 font-black shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all">
                  UPDATE PROFILE DETAILS
                </button>
              </form>
            </div>
          </div>
        </div>

      </main>

      <!-- RIGHT SIDEBAR: SEARCH & NEWS -->
      <aside class="col-span-1 p-6 hidden lg:flex flex-col gap-6 sticky top-0 h-screen overflow-y-auto bg-white">
        
        <!-- Search -->
        <div class="border-4 border-black p-3 shadow-brutal bg-amber-50 flex items-center">
          <span class="mr-2 font-black text-sm">🔍</span>
          <input type="text" v-model="searchKeyword" placeholder="[ SEARCH TIMELINE ]..." class="w-full outline-none font-bold bg-transparent placeholder-gray-500" />
          <button v-if="searchKeyword" @click="searchKeyword = ''" class="font-black text-xs text-red-500 hover:underline">CLEAR</button>
        </div>

        <!-- Today's News -->
        <div class="border-4 border-black shadow-brutal bg-white flex flex-col min-h-0 flex-grow">
          <h2 class="font-black bg-black text-amber-300 p-3 text-center border-b-2 border-black">
            AI TODAY'S NEWS
          </h2>
          
          <div class="p-3 bg-amber-100 border-b-2 border-black flex justify-between items-center shrink-0">
            <span class="text-xs font-black text-black">Timeline Highlights</span>
            <button @click="handleGenerateNews" class="border-2 border-black bg-amber-300 px-2 py-0.5 text-xs font-black shadow-brutal-sm hover:translate-y-[1px] hover:translate-x-[1px] hover:shadow-none transition-all">
              ⚡ SUMMARIZE
            </button>
          </div>

          <!-- News Scroll Box -->
          <div class="p-4 flex flex-col gap-4 overflow-y-auto flex-grow bg-amber-50/50">
            <div v-if="newsList.length === 0" class="text-center font-bold text-gray-500 text-xs py-10">
              No news summarized yet. Click "SUMMARIZE" above to generate news highlights from timeline posts!
            </div>
            
            <div v-for="n in newsList" :key="n.id" class="border-2 border-black bg-white p-3 shadow-brutal-sm relative">
              <span class="text-[10px] font-black text-amber-600 block mb-1">AI GENERATED • {{ formatTime(n.tanggalPublic) }}</span>
              <h4 class="font-black text-sm mb-1 text-black leading-tight">{{ n.judul }}</h4>
              <p class="text-[11px] font-bold text-gray-500 mb-2 leading-snug">{{ n.deskripsi }}</p>
              <div class="text-xs font-medium text-gray-700 leading-relaxed border-t border-black/10 pt-2 markdown-content" v-html="marked.parse(n.konten)"></div>
            </div>
          </div>
        </div>

      </aside>

    </div>
  </div>
</template>

<style>
/* Neo-brutalist helper classes */
.shadow-brutal {
  box-shadow: 4px 4px 0px 0px #000000;
}
.shadow-brutal-sm {
  box-shadow: 2px 2px 0px 0px #000000;
}
.shadow-brutal-lg {
  box-shadow: 8px 8px 0px 0px #000000;
}

/* Parsed Markdown Styles */
.markdown-content p {
  margin-bottom: 0.75rem;
  line-height: 1.5;
}
.markdown-content p:last-child {
  margin-bottom: 0;
}
.markdown-content ul {
  margin-left: 1.25rem;
  margin-bottom: 0.75rem;
  list-style-type: disc;
}
.markdown-content ol {
  margin-left: 1.25rem;
  margin-bottom: 0.75rem;
  list-style-type: decimal;
}
.markdown-content li {
  margin-bottom: 0.25rem;
}
.markdown-content strong {
  font-weight: 900;
  color: #000000;
}
.markdown-content em {
  font-style: italic;
}
</style>
