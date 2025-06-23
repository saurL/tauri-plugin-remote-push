use serde::de::DeserializeOwned;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

use crate::models::*;

pub fn init<R: Runtime, C: DeserializeOwned>(
  app: &AppHandle<R>,
  _api: PluginApi<R, C>,
  _config: Option<Config>,
) -> crate::Result<RemotePush<R>> {
  Ok(RemotePush(app.clone()))
}

/// Access to the remote-push APIs.
pub struct RemotePush<R: Runtime>(AppHandle<R>);

impl<R: Runtime> RemotePush<R> {
  pub fn get_token(&self) -> crate::Result<String> {
    Ok("".to_string())
  }

  pub async fn request_permission(&self) -> crate::Result<PermissionState> {
    Ok(PermissionState { granted: true })
  }
}
