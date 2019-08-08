(ns status-im.ui.screens.profile.user.sheet.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.utils.utils :as utils]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn profile-icon-actions [include-remove-action?]
  (fn []
    [react/view {:flex 1 :flex-direction :row}
     [react/view action-button.styles/actions-list
      [action-button/action-button
       {:label               (i18n/label :t/image-source-gallery)
        :accessibility-label :group-info-button
        :icon                :main-icons/photo
        :icon-opts           {:color colors/blue}
        :on-press            #(hide-sheet-and-dispatch [:my-profile/update-picture])}]
      [action-button/action-button
       {:label               (i18n/label :t/image-source-make-photo)
        :accessibility-label :delete-and-leave-group-button
        :icon                :main-icons/camera
        :icon-opts           {:color colors/blue}
        :on-press            (fn []
                               (hide-sheet-and-dispatch
                                [:request-permissions
                                 {:permissions [:camera :write-external-storage]
                                  :on-allowed  #(re-frame/dispatch
                                                 [:navigate-to :profile-photo-capture])
                                  :on-denied
                                  (fn []
                                    (utils/set-timeout
                                     #(utils/show-popup (i18n/label :t/error)
                                                        (i18n/label :t/camera-access-error))
                                     50))}]))}]
      (when include-remove-action?
        [action-button/action-button
         {:label               (i18n/label :t/image-remove-current)
          :label-style         {:color colors/red}
          :accessibility-label :group-info-button
          :icon                :main-icons/delete
          :icon-opts           {:color colors/red}
          :cyrcle-color        colors/red-light
          :on-press            #(hide-sheet-and-dispatch [:my-profile/remove-current-photo])}])]]))
