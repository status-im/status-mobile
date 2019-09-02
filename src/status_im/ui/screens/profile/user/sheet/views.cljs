(ns status-im.ui.screens.profile.user.sheet.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.list-item.views :as list-item]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn profile-icon-actions [include-remove-action?]
  (fn []
    [react/view {:flex 1}
     [list-item/list-item
      {:theme               :action
       :title               :t/image-source-gallery
       :accessibility-label :group-info-button
       :icon                :main-icons/photo
       :on-press            #(hide-sheet-and-dispatch [:my-profile/update-picture])}]
     [list-item/list-item
      {:theme               :action
       :title               :t/image-source-make-photo
       :accessibility-label :delete-and-leave-group-button
       :icon                :main-icons/camera
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
       [list-item/list-item
        {:theme               :action-destructive
         :title               :t/image-remove-current
         :accessibility-label :group-info-button
         :icon                :main-icons/delete
         :on-press            #(hide-sheet-and-dispatch [:my-profile/remove-current-photo])}])]))
