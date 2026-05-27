<template>
  <div
    class="min-h-screen bg-white text-black font-sans selection:bg-black selection:text-white"
  >
    <div
      class="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-4 min-h-screen border-x-4 border-black"
    >
      <aside
        class="col-span-1 border-r-4 border-black p-6 flex flex-col justify-between sticky top-0 h-screen"
      >
        <div>
          <div
            class="bg-black text-white text-2xl font-bold p-3 inline-block mb-10"
          >
            [ CHIRP ]
          </div>

          <nav class="flex flex-col gap-6 font-bold text-lg">
            <a href="#" class="flex items-center gap-3 hover:underline">
              <span class="border-2 border-black px-2 py-0.5 text-sm">H</span>
              Home
            </a>
            <a href="#" class="flex items-center gap-3 hover:underline">
              <span class="border-2 border-black px-2 py-0.5 text-sm">N</span>
              Notifications
            </a>
            <a href="#" class="flex items-center gap-3 hover:underline">
              <span class="border-2 border-black px-2 py-0.5 text-sm">M</span>
              Messages
            </a>
            <a href="#" class="flex items-center gap-3 hover:underline">
              <span class="border-2 border-black px-2 py-0.5 text-sm">U</span>
              Profile
            </a>
          </nav>
        </div>

        <button
          class="bg-white border-4 border-black py-3 font-bold text-lg shadow-brutal hover:translate-y-[3px] hover:translate-x-[3px] hover:shadow-brutal-sm transition-all active:shadow-none active:translate-y-[5px] active:translate-x-[5px]"
        >
          POST
        </button>
      </aside>

      <main class="col-span-2 border-r-4 border-black bg-white">
        <header
          class="border-b-4 border-black p-4 font-bold text-xl flex justify-between items-center sticky top-0 bg-white z-10"
        >
          <span>[ FOR YOU ]</span>
          <button
            class="border-2 border-black px-3 py-1 text-xs shadow-brutal hover:translate-y-[2px] hover:translate-x-[2px] hover:shadow-brutal-sm transition-all"
          >
            CLEAR CONTEXT
          </button>
        </header>

        <div class="p-6">
          <div class="border-4 border-black p-4 shadow-brutal mb-6">
            <p class="font-bold text-gray-500 mb-4">
              [ GENERATING RESPONSE... ]
            </p>
            <p class="font-bold">
              Area ini nantinya akan digunakan untuk me-render komponen Card
              Postingan dari Nuxt Pages.
            </p>
          </div>
        </div>
      </main>

      <aside
        class="col-span-1 p-6 hidden lg:block sticky top-0 h-screen overflow-y-auto"
      >
        <div class="border-4 border-black p-2 mb-8 shadow-brutal">
          <input
            type="text"
            placeholder="[ SEARCH ] ..."
            class="w-full outline-none font-bold bg-transparent"
          />
        </div>

        <div class="border-4 border-black shadow-brutal">
          <h2 class="font-bold bg-black text-white p-2 text-center">
            TODAY'S NEWS
          </h2>
          <div class="p-4 flex flex-col gap-4">
            <div class="border-b-4 border-black pb-4 border-dashed">
              <span class="text-xs font-bold text-gray-500"
                >TECHNOLOGY • 23 HOURS AGO</span
              >
              <p class="font-bold mt-1">
                Anthropic Closes In on OpenAI as U.S. Business Adoption Surges
              </p>
            </div>
            <div>
              <span class="text-xs font-bold text-gray-500"
                >OTHER • 6 HOURS AGO</span
              >
              <p class="font-bold mt-1">Anthropic Unveils Mythos Preview</p>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
