(ns status-im.ui.screens.profile.edit.views
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :refer [dispatch]]
            [reagent.core :as reagent]
            [status-im.components.camera :as camera]
            [status-im.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.components.react :as react]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.sticky-button :refer [sticky-button]]
            [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.i18n :refer [label]]
            [status-im.ui.screens.profile.db :as db]
            [status-im.ui.screens.profile.events :as profile.events]
            [status-im.ui.screens.profile.styles :as styles]
            [status-im.ui.screens.profile.views :refer [colorize-status-hashtags]]
            [status-im.utils.utils :as utils :refer [clean-text]])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn edit-my-profile-toolbar []
  [toolbar {:title   (label :t/edit-profile)
            :actions [{:image :blank}]}])

(defview profile-name-input []
  (letsubs [new-profile-name [:get-in [:profile-edit :name]]]
    [react/view
     [text-input-with-label {:label          (label :t/name)
                             :default-value  new-profile-name
                             :on-change-text #(dispatch [:set-in [:profile-edit :name] %])}]]))

(def profile-icon-options
  [{:text  (label :t/image-source-gallery)
    :value #(dispatch [:open-image-picker])}
   {:text  (label :t/image-source-make-photo)
    :value (fn []
             (dispatch [:request-permissions
                        [:camera :write-external-storage]
                        (fn []
                          (camera/request-access
                           #(if % (dispatch [:navigate-to :profile-photo-capture])
                                (utils/show-popup (label :t/error)
                                                  (label :t/camera-access-error)))))]))}])

(defn edit-profile-bage [contact]
  [react/view styles/edit-profile-bage
   [react/view styles/edit-profile-icon-container
    [context-menu
     [my-profile-icon {:account contact
                       :edit?   true}]
     profile-icon-options
     styles/context-menu-custom-styles]]
   [react/view styles/edit-profile-name-container
    [profile-name-input]]])

(defn edit-profile-status [{:keys [status edit-status?]}]
  (let [input-ref (reagent/atom nil)]
    [react/view styles/edit-profile-status
     [react/scroll-view
      (if edit-status?
        [react/text-input
         {:ref               #(reset! input-ref %)
          :auto-focus        edit-status?
          :multiline         true
          :max-length        140
          :placeholder       (label :t/status)
          :style             styles/profile-status-input
          :on-change-text    #(dispatch [:set-in [:profile-edit :status] (clean-text %)])
          :on-blur           #(dispatch [:set-in [:profile-edit :edit-status?] false])
          :blur-on-submit    true
          :on-submit-editing #(.blur @input-ref)
          :default-value     status}]
        [react/touchable-highlight {:on-press #(dispatch [:set-in [:profile-edit :edit-status?] true])}
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
  (letsubs [current-account [:get-current-account]
            changed-account [:get :profile-edit]]
    {:component-will-unmount #(dispatch [:set-in [:profile-edit :edit-status?] false])}
    (let [profile-edit-data-valid? (spec/valid? ::db/profile changed-account)
          profile-edit-data-changed? (or (not= (:name current-account) (:name changed-account))
                                         (not= (:status current-account) (:status changed-account))
                                         (not= (:photo-path current-account) (:photo-path changed-account)))]
      [react/keyboard-avoiding-view {:style styles/profile}
       [status-bar]
       [edit-my-profile-toolbar]
       [react/view styles/edit-my-profile-form
        [edit-profile-bage changed-account]
        [edit-profile-status changed-account]
        [status-prompt changed-account]]
       (when (and profile-edit-data-changed? profile-edit-data-valid?)
         [sticky-button (label :t/save) #(do
                                           (dispatch [:check-status-change (:status changed-account)])
                                           (dispatch [:account-update changed-account]))])])))
