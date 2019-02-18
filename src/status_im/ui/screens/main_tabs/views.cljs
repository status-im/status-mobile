(ns status-im.ui.screens.main-tabs.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar.view]
            [status-im.ui.components.styles :as common.styles]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.wallet.main.views :as wallet.main]
            [status-im.ui.screens.profile.user.views :as profile.user]))

(views/defview main-container [view-id]
  ;; :should-component-update is called only when props are changed,
  ;; that's why view-id is passed as a prop here. main-tabs component will be
  ;; rendered while next screen from stack navigator is shown, so we have
  ;; to prevent re-rendering to avoid no clause exception in case form
  {:should-component-update
   (fn [_ _ [_ new-view-id]]
     (contains? #{:home :wallet :my-profile} new-view-id))}
  [react/view common.styles/main-container
   (case view-id
     :home [home/home-wrapper]
     :wallet [wallet.main/wallet]
     :my-profile [profile.user/my-profile]
     nil)])

(defn main-tabs [view-id]
  [react/view common.styles/flex
   [status-bar.view/status-bar
    {:type (if (= view-id :wallet) :wallet-tab :main)}]
   [main-container view-id]])

(defn get-main-tab [view-id]
  (fn []
    [main-tabs view-id]))
