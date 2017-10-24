(ns status-im.ui.components.drawer.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.ui.components.chat-icon.screen :as ci]
            [status-im.ui.components.common.common :as common]
            [status-im.ui.components.context-menu :as context-menu]
            [status-im.ui.components.drawer.styles :as st]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.status-view.view :as status-view]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.db :as profile.db]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.utils.money :as money]
            [status-im.protocol.core :as protocol]))

(defonce drawer-atom (atom nil))
(defn open-drawer! [] (.openDrawer @drawer-atom))
(defn close-drawer! [] (.closeDrawer @drawer-atom))

;; the save-event subscription is here because no save action would
;; be dispatched when the user presses a screen changing zone while
;; editing the name or status field

(defn save-profile! []
  (when-let [save-event @(re-frame/subscribe [:my-profile.drawer/save-event])]
    (re-frame/dispatch [save-event])))

(defn navigate-to-profile []
  (close-drawer!)
  (save-profile!)
  (re-frame/dispatch [:navigate-to :my-profile]))

(defn navigate-to-accounts []
  (close-drawer!)
  (save-profile!)
  ;; TODO(rasom): probably not the best place for this call
  (protocol/stop-whisper!)
  (re-frame/dispatch [:navigate-to :accounts]))

(defview profile-picture []
  (letsubs [account [:get-current-account]]
    [react/touchable-opacity {:on-press navigate-to-profile
                              :style    st/user-photo-container}
     [react/view
      [ci/chat-icon (:photo-path account) {:size                52
                                           :accessibility-label :drawer-profile-icon}]]]))

(defview name-input []
  (letsubs [profile-name [:my-profile.drawer/get :name]
            valid-name?  [:my-profile.drawer/valid-name?]
            placeholder  [:get :my-profile/default-name]]
    [react/view st/name-input-wrapper
     [react/text-input
      {:placeholder    placeholder
       :style          (st/name-input-text valid-name?)
       :font           :medium
       :default-value  profile-name
       :on-focus       #(re-frame/dispatch [:my-profile.drawer/edit-name])
       :on-change-text #(re-frame/dispatch [:my-profile.drawer/update-name %])
       :on-end-editing #(re-frame/dispatch [:my-profile.drawer/save-name])}]]))

(defview status-input []
  (letsubs [edit-status? [:my-profile.drawer/get :edit-status?]
            status       [:my-profile.drawer/get :status]]
    (let [placeholder (i18n/label :t/update-status)]
      [react/view st/status-container
       (if edit-status?
         [react/text-input {:style               st/status-input-view
                            :multiline           true
                            :auto-focus          true
                            :focus               edit-status?
                            :max-length          140
                            :accessibility-label :drawer-status-input
                            :placeholder         placeholder
                            :default-value       status
                            :on-change-text      #(re-frame/dispatch [:my-profile.drawer/update-status %])
                            :on-end-editing      #(when edit-status?
                                                    (re-frame/dispatch [:my-profile.drawer/save-status]))}]
         [status-view/status-view {:style           (st/status-view (string/blank? status))
                                   :number-of-lines 3
                                   :status          (if (string/blank? status)
                                                      placeholder
                                                      status)
                                   :on-press        #(re-frame/dispatch [:my-profile.drawer/edit-status])}])])))

(defview current-network []
  (letsubs [network [:get-current-account-network]]
    [react/view {:style st/network-label-container}
     [react/text {:style st/network-label} (i18n/label :t/current-network)]
     [react/text {:style st/network-title} (:name network)]]))

(defn options-btn []
  [react/view {:style st/options-button}
   [react/touchable-highlight {:on-press navigate-to-profile}
    [react/view [vi/icon :icons/options]]]])

(defn switch-account []
  [react/view st/switch-account-container
   [react/touchable-highlight
    {:on-press navigate-to-accounts}
    [react/view
     [react/text {:style      st/switch-account-text
                  :font       (if platform/android? :medium :default)
                  :uppercase? platform/android?}
      (i18n/label :t/switch-users)]]]])

(defn drawer []
  (fn []
    [react/touchable-without-feedback {:on-press #(react/dismiss-keyboard!)}
     [react/view st/drawer
      [react/view st/upper-container
       [react/view st/profile-container
        [profile-picture]
        [name-input]
        [status-input]
        [options-btn]]
       [current-network]]
      [react/view
       [switch-account]]]]))

(defn drawer-view [items]
  [react/drawer-layout {:drawerWidth          300
                        :renderNavigationView #(reagent/as-element [drawer])
                        :onDrawerSlide        react/dismiss-keyboard!
                        :ref                  (fn [drawer]
                                                (reset! drawer-atom drawer))}
   items])
