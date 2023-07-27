(ns status-im2.contexts.profile.create.events
  (:require [utils.re-frame :as rf]
            [native-module.core :as native-module]
            [status-im2.contexts.profile.config :as profile.config]
            [status-im.ethereum.core :as ethereum]
            [utils.security.core :as security]
            [re-frame.core :as re-frame]
            [status-im2.contexts.shell.jump-to.utils :as shell.utils]
            [status-im.transport.core :as transport]
            [status-im.communities.core :as communities]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.contexts.chat.messages.link-preview.events :as link-preview]
            [status-im2.common.log :as logging]
            [status-im2.navigation.events :as navigation]))

(re-frame/reg-fx
 ::create-profile-and-login
 (fn [request]
   ;;"node.login" signal will be triggered as a callback
   (native-module/create-account-and-login request)))

(rf/defn create-profile-and-login
  {:events [:profile.create/create-and-login]}
  [_ {:keys [display-name password image-path color]}]
  {::create-profile-and-login
   (merge (profile.config/create)
          {:displayName        display-name
           :password           (ethereum/sha3 (security/safe-unmask-data password))
           :imagePath          (profile.config/strip-file-prefix image-path)
           :customizationColor color})})

(rf/defn login-new-profile
  [{:keys [db] :as cofx} recovered-account?]
  (let [{:profile/keys  [profile profiles-overview wallet-accounts]
         :networks/keys [current-network networks]} db
        network-id
        (str (get-in networks [current-network :config :NetworkId]))]
    (shell.utils/change-selected-stack-id :communities-stack true nil)
    (rf/merge cofx
              {:db                (assoc db :chats/loading? false)
               :wallet/get-tokens [network-id wallet-accounts recovered-account?]}
              (transport/start-messenger)
              (communities/fetch)
              (data-store.chats/fetch-chats-preview
               {:on-success #(re-frame/dispatch [:chats-list/load-success %])})
              (multiaccounts/switch-preview-privacy-mode-flag)
              (link-preview/request-link-preview-whitelist)
              (logging/set-log-level (:log-level profile))
              (navigation/init-root :enable-notifications))))
