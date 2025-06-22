import { invoke, type PluginListener } from '@tauri-apps/api/core'

export interface PushNotification {
  title?: string;
  body?: string;
  data: Record<string, any>;
  badge?: number;
  sound?: string;
  channelId?: string; // Android
  category?: string;  // iOS
}

// Core Functions
export async function getToken(): Promise<string> {
  return await invoke("plugin:remote-push|get_token");
}

export async function requestPermission(): Promise<{ granted: boolean }> {
  return await invoke("plugin:remote-push|request_permission");
}

// Event Listeners
export async function onNotificationReceived(
  handler: (notification: PushNotification) => void
): Promise<PluginListener> {
  return await invoke("plugin:remote-push|on_notification_received", { handler });
}

export async function onTokenRefresh(
  handler: (token: string) => void
): Promise<PluginListener> {
  return await invoke("plugin:remote-push|on_token_refresh", { handler });
}

export async function onNotificationTapped(
  handler: (notification: PushNotification) => void
): Promise<PluginListener> {
  return await invoke("plugin:remote-push|on_notification_tapped", { handler });
}
