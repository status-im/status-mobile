(ns status-im.ui.screens.profile.edit.views
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as reagent]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.ui.components.context-menu :refer [context-menu]]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :refer [status-bar]]
            [status-im.ui.components.sticky-button :refer [sticky-button]]
            [status-im.ui.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.db :as db]
            [status-im.ui.screens.profile.events :as profile.events]
            [status-im.ui.screens.profile.styles :as styles]
            [status-im.ui.screens.profile.views :refer [colorize-status-hashtags]]
            [status-im.utils.utils :as utils :refer [clean-text]])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn edit-my-profile-toolbar []
  [toolbar/toolbar {}
   toolbar/default-nav-back
   [toolbar/content-title (label :t/edit-profile)]])

(defview profile-name-input []
  (letsubs [profile-name [:my-profile/get :name]
            placeholder  [:get :my-profile/default-name]]
    [react/view
     [text-input-with-label
      {:label          (label :t/name)
       :default-value  profile-name
       :on-focus       #(dispatch [:my-profile/edit-profile])
       :on-change-text #(dispatch [:my-profile/update-name %])}]]))


(def profile-icon-options
  [{:text  (label :t/image-source-gallery)
    :value #(dispatch [:my-profile/update-picture])}
   {:text  (label :t/image-source-make-photo)
    :value (fn []
             (dispatch [:request-permissions
                        [:camera :write-external-storage]
                        (fn []
                          (camera/request-access
                           #(if %
                              (dispatch [:navigate-to :profile-photo-capture])
                              (utils/show-popup (label :t/error)
                                                (label :t/camera-access-error)))))]))}])

(defn edit-profile-badge [contact]
  [react/view styles/edit-profile-badge
   [react/view styles/edit-profile-icon-container
    [context-menu
     [my-profile-icon {:account contact
                       :edit?   true}]
     profile-icon-options
     styles/context-menu-custom-styles]]
   [react/view styles/edit-profile-name-container
    [profile-name-input]]])

(defview edit-profile-status []
  (letsubs [edit-status? [:my-profile/get :edit-status?]
            status       [:my-profile/get :status]]
    [react/view styles/edit-profile-status
     [react/scroll-view
      (if edit-status?
        [react/text-input
         {:auto-focus        (if edit-status? true false)
          :multiline         true
          :max-length        140
          :placeholder       (label :t/status)
          :style             styles/profile-status-input
          :on-change-text    #(dispatch [:my-profile/update-status %])
          :default-value     status}]
        [react/touchable-highlight {:on-press #(dispatch [:my-profile/edit-profile :status])}
         [react/view
          (if (string/blank? status)
            [react/text {:style styles/add-a-status}
             (label :t/status)]
            [react/text {:style styles/profile-status-text}
             (colorize-status-hashtags status)])]])]]))

(defn status-prompt [{:keys [status]}]
  (when (or (nil? status) (string/blank? status))
    [react/view styles/status-prompt
     [react/text {:style styles/status-prompt-text}
      (colorize-status-hashtags (label :t/status-prompt))]]))

(defview edit-my-profile []
  (letsubs [current-account  [:get-current-account]
            changed-account  [:get :my-profile/profile]
            profile-changed? [:my-profile/changed?]
            valid-name?      [:my-profile/valid-name?]]
    [react/keyboard-avoiding-view {:style styles/profile}
     [status-bar]
     [edit-my-profile-toolbar]
     [react/scroll-view styles/edit-my-profile-form
      [edit-profile-badge changed-account]
      [edit-profile-status]
      [status-prompt changed-account]]
     (when (and valid-name? profile-changed?)
       [sticky-button (label :t/save) #(dispatch [:my-profile/save-profile])])]))
