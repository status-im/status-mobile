(ns status-im.chat.models.link-preview
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]))

(fx/defn enable
  {:events [::enable]}
  [{{:keys [multiaccount]} :db :as cofx} site enabled?]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :link-previews-enabled-sites
             (if enabled?
               (conj (get multiaccount :link-previews-enabled-sites #{}) site)
               (disj (get multiaccount :link-previews-enabled-sites #{}) site))
             {})))

(fx/defn load-link-preview-data
  {:events [::load-link-preview-data]}
  [cofx link]
  (fx/merge cofx
            {::json-rpc/call [{:method     (json-rpc/call-ext-method "getLinkPreviewData")
                               :params     [link]
                               :on-success #(re-frame/dispatch [::cache-link-preview-data link %])
                               :on-error   #(log/error "Can't get preview data for " link)}]}))

(fx/defn cache-link-preview-data
  {:events [::cache-link-preview-data]}
  [{{:keys [multiaccount]} :db :as cofx} site {:keys [error] :as data}]
  (when-not error
    (multiaccounts.update/optimistic
     cofx
     :link-previews-cache
     (assoc (get multiaccount :link-previews-cache {}) site data))))

(fx/defn should-suggest-link-preview
  {:events [::should-suggest-link-preview]}
  [{:keys [db] :as cofx} enabled?]
  (multiaccounts.update/multiaccount-update
   cofx
   :link-preview-request-enabled (boolean enabled?)
   {}))

(fx/defn request-link-preview-whitelist
  [_]
  {::json-rpc/call [{:method     (json-rpc/call-ext-method "getLinkPreviewWhitelist")
                     :params     []
                     :on-success #(re-frame/dispatch [::link-preview-whitelist-received %])
                     :on-error   #(log/error "Failed to get link preview whitelist")}]})

(fx/defn save-link-preview-whitelist
  {:events [::link-preview-whitelist-received]}
  [cofx whitelist]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :link-previews-whitelist whitelist {})))

