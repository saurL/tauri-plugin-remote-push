use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, serde::Deserialize)]
pub struct Config {
    #[serde(rename = "senderId")]
    pub sender_id: Option<String>,
}

#[derive(Debug, Clone, Default, Deserialize, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct PermissionState {
    pub granted: bool,
}
