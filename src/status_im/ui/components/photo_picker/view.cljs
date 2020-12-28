(ns status-im.ui.components.photo-picker.view
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.permissions :as permissions]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]))

(re-frame/reg-fx
 ::pick-photos
 (fn [{:keys [on-success options]}]
   (react/show-image-picker on-success options)))

(re-frame/reg-fx
 ::take-photos
 (fn [{:keys [on-success options]}]
   (react/show-image-picker-camera on-success options)))

(fx/defn -pick-photos
  {:events [::pick-photos]}
  [cofx on-success options]
  {::pick-photos {:on-success on-success
                  :options    options}})

(fx/defn -take-photos
  {:events [::take-photos]}
  [cofx on-success options]
  {::take-photos {:on-success on-success
                  :options    options}})

(defn take-picture [on-success options]
  (permissions/request-permissions
   {:permissions [:camera]
    :on-allowed  #(re-frame/dispatch [::take-photos on-success options])
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup (i18n/label :t/error)
                                       (i18n/label :t/camera-access-error))
                    50))}))

(defn show-image-picker [on-success options]
  (permissions/request-permissions
   {:permissions [:read-external-storage :write-external-storage :photo-library]
    :on-allowed  #(re-frame/dispatch [::pick-photos on-success options])
    :on-denied   (fn []
                   (utils/set-timeout
                    #(utils/show-popup (i18n/label :t/error)
                                       (i18n/label :t/external-storage-denied))
                    50))}))

