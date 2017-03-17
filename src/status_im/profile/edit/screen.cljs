(ns status-im.profile.edit.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.profile.styles :as st]
            [status-im.components.styles :refer [color-blue color-gray5]]
            [re-frame.core :refer [dispatch]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.toolbar-new.view :refer [toolbar]]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.text-field.view :refer [text-field]]
            [status-im.i18n :refer [label]]
            [status-im.components.confirm-button :refer [confirm-button]]
            [status-im.components.chat-icon.screen :refer [my-profile-icon]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.profile.validations :as v]
            [status-im.components.react :refer [view
                                                scroll-view
                                                text
                                                touchable-highlight
                                                text-input]]
            [cljs.spec :as s]))

(defn edit-my-profile-toolbartoolbar []
  [toolbar {:title   (label :t/edit-profile)
            :actions [{:image :blank}]}])

(defview profile-name-input []
  [new-profile-name [:get-in [:profile-edit :name]]]
  [view
   [text-field
    {:wrapper-style     st/profile-name-wrapper
     :line-color        st/edit-line-color
     :focus-line-color  st/profile-focus-line-color
     :focus-line-height st/profile-focus-line-height
     :label-hidden?     true
     :input-style       st/profile-name-input
     :on-change-text    #(dispatch [:set-in [:profile-edit :name] %])
     :value             new-profile-name}]])

(def profile-icon-options
  [{:text (label :t/image-source-gallery)    :value #(dispatch [:open-image-picker])}
   {:text (label :t/image-source-make-photo) :value #(dispatch [:navigate-to :profile-photo-capture])}])

(defn edit-profile-bage [contact]
  [view st/edit-profile-bage
   [view
    [context-menu
     [my-profile-icon {:account contact
                       :edit?   true}]
     profile-icon-options]]
   [view st/edit-profile-name-container
    [text {:style st/edit-name-title}
     (label :t/name)]
    [profile-name-input]]])

(defn edit-profile-status [{:keys [status]}]
  [view st/edit-profile-status
   [view
    [text {:style st/edit-status-title} "Status"]
    [view
     [text-field
      {:wrapper-style     st/profile-status-wrapper
       :line-color        st/edit-line-color
       :focus-line-color  st/profile-focus-line-color
       :focus-line-height st/profile-focus-line-height
       :multiline         true
       :max-length        140
       :placeholder       (label :t/profile-no-status)
       :label-hidden?     true
       :input-style       st/profile-status-input
       :on-change-text    #(dispatch [:set-in [:profile-edit :status] %])
       :value             status}]]]])

(defview edit-my-profile []
  [current-account [:get-current-account]
   changed-account [:get :profile-edit]]
  (let [profile-edit-data-valid? (s/valid? ::v/profile changed-account)
        profile-edit-data-changed? (or (not= (:name current-account) (:name changed-account))
                                       (not= (:status current-account) (:status changed-account))
                                       (not= (:photo-path current-account) (:photo-path changed-account)))]
    [view st/profile
     [status-bar]
     [edit-my-profile-toolbartoolbar]
     [view {:flex 1}
      [scroll-view st/edit-my-profile-form
       [edit-profile-bage changed-account]
       [edit-profile-status changed-account]]]
     (when (and profile-edit-data-changed? profile-edit-data-valid?)
       [confirm-button (label :t/save) #(do
                                          (dispatch [:check-status-change (:status changed-account)])
                                          (dispatch [:account-update changed-account]))])]))
