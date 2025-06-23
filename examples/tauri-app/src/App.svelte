<script>
  import Greet from './lib/Greet.svelte'
  import {
    getToken,
    requestPermission,
    onNotificationReceived,
    onTokenRefresh,
    onNotificationTapped
  } from '../../../guest-js'

	let response = ''

	function updateResponse(returnValue) {
		response += `[${new Date().toLocaleTimeString()}] ` + (typeof returnValue === 'string' ? returnValue : JSON.stringify(returnValue)) + '<br>'
	}

	function _getToken() {
		getToken().then(updateResponse).catch(updateResponse)
	}

  function _requestPermission() {
    requestPermission().then(updateResponse).catch(updateResponse)
	}

  onNotificationReceived(updateResponse);
  onTokenRefresh(updateResponse);
  onNotificationTapped(updateResponse);

</script>

<main class="container">
  <h1>Welcome to Tauri!</h1>

  <div class="row">
    <a href="https://vite.dev" target="_blank">
      <img src="/vite.svg" class="logo vite" alt="Vite Logo" />
    </a>
    <a href="https://tauri.app" target="_blank">
      <img src="/tauri.svg" class="logo tauri" alt="Tauri Logo" />
    </a>
    <a href="https://svelte.dev" target="_blank">
      <img src="/svelte.svg" class="logo svelte" alt="Svelte Logo" />
    </a>
  </div>

  <p>
    Click on the Tauri, Vite, and Svelte logos to learn more.
  </p>

  <div class="row">
    <Greet />
  </div>

  <div>
    <button on:click="{_getToken}">Get Token</button>
    <button on:click="{_requestPermission}">Request Permission</button>
    <div>{@html response}</div>
  </div>

</main>

<style>
  .logo.vite:hover {
    filter: drop-shadow(0 0 2em #747bff);
  }

  .logo.svelte:hover {
    filter: drop-shadow(0 0 2em #ff3e00);
  }
</style>
