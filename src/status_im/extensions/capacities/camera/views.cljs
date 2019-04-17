(ns status-im.extensions.capacities.camera.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.photo-capture.styles :as styles]
            [status-im.utils.image-processing :as image-processing]))

(def default-max-size 1024)

(defn- process-image-and-finish [path context]
  (let [on-success #(re-frame/dispatch [:extensions/camera-picture-taken % context])
        on-error   #(re-frame/dispatch [:extensions/camera-error %2 context])]
    (image-processing/img->base64 path on-success on-error default-max-size default-max-size)))

(defview take-picture []
  (letsubs [context [:get-screen-params]
            camera-ref (reagent/atom false)]
    [react/view styles/container
     [status-bar/status-bar]
     [toolbar/toolbar nil
      [toolbar/nav-button (actions/back
                           #(do
                              (re-frame/dispatch [:extensions/camera-cancel context])
                              (re-frame/dispatch [:navigate-back])))]
      [toolbar/content-title (i18n/label :t/extensions-camera-send-picture)]]
     [camera/camera {:style         {:flex 1}
                     :aspect        (:fill camera/aspects)
                     :captureTarget (:disk camera/capture-targets)
                     :ref           #(reset! camera-ref %)}]
     [react/view styles/button-container
      [react/view styles/button
       [react/touchable-highlight {:on-press (fn []
                                               (let [camera @camera-ref]
                                                 (-> (.capture camera)
                                                     (.then #(process-image-and-finish (.-path %) context))
                                                     (.catch #(re-frame/dispatch [:extensions/camera-error % context])))))}
        [react/view
         [icons/icon :main-icons/camera {:color :white}]]]]]]))

;TODO image picker doesn't notify when the user cancel image selection
; in this case we cannot notify cancel to extension 
(defn- open-image-picker [context]
  (react/show-image-picker
   (fn [image]
     (let [path (get (js->clj image) "path")]
       (process-image-and-finish path context)))
   "photo"))

(defn pick-or-take-picture-list-selection [context]
  {:title   (i18n/label :t/extensions-camera-send-picture)
   :options [{:label  (i18n/label :t/image-source-gallery)
              :action #(open-image-picker context)}
             {:label  (i18n/label :t/image-source-make-photo)
              :action (fn []
                        (re-frame/dispatch [:request-permissions {:permissions [:camera :write-external-storage]
                                                                  :on-allowed  #(re-frame/dispatch [:navigate-to :take-picture (merge context {:back? true})])
                                                                  :on-denied   #(re-frame/dispatch [:extensions/camera-denied context])}]))}]
   :on-cancel #(re-frame/dispatch [:extensions/camera-cancel context])})
