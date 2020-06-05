(ns status-im.ui.screens.qr-scanner.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.ui.components.camera :as camera]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as topbar]
            [status-im.ui.screens.qr-scanner.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.config :as config]
            [status-im.ui.components.button :as button]
            [reagent.core :as reagent]))

(defn- topbar [_ {:keys [title] :as opts}]
  [topbar/toolbar
   {:transparent? true}
   [topbar/nav-text
    {:style   {:color colors/white-persist :margin-left 16}
     :handler #(re-frame/dispatch [:qr-scanner.callback/scan-qr-code-cancel opts])}
    (i18n/label :t/cancel)]
   [topbar/content-title {:color colors/white-persist}
    (or title (i18n/label :t/scan-qr))]
   #_[topbar/actions [{:icon      (if (= :on camera-flashlight)
                                    :main-icons/flash-active
                                    :main-icons/flash-inactive)
                       :icon-opts {:color colors/white}
                       :handler   #(re-frame/dispatch [:wallet/toggle-flashlight])}]]])

(defn qr-test-view [opts]
  (let [text-value (atom "")]
    [react/safe-area-view {:style {:flex             1
                                   :background-color colors/black-persist}}
     [topbar nil opts]
     [react/view {:flex            1
                  :align-items     :center
                  :justify-content :center}
      [react/text-input {:multiline      true
                         :style {:color colors/white-persist}
                         :on-change-text #(reset! text-value %)}]
      [react/view {:flex-direction :row}
       [button/button
        {:label    "Cancel"
         :on-press #(re-frame/dispatch [:qr-scanner.callback/scan-qr-code-cancel opts])}]
       [button/button
        {:label    "OK"
         :on-press #(re-frame/dispatch [:qr-scanner.callback/scan-qr-code-success opts (when-let [text @text-value] (string/trim text))])}]]]]))

(defn corner [border1 border2 corner]
  [react/view (assoc {:border-color colors/white-persist :width 60 :height 60} border1 5 border2 5 corner 32)])

(defn- viewfinder [size]
  [react/view {:style styles/viewfinder-port}
   [react/view {:width size :height size :justify-content :space-between}
    [react/view {:flex-direction :row :justify-content :space-between}
     [corner :border-top-width :border-left-width :border-top-left-radius]
     [corner :border-top-width :border-right-width :border-top-right-radius]]
    [react/view {:flex-direction :row :justify-content :space-between}
     [corner :border-bottom-width :border-left-width :border-bottom-left-radius]
     [corner :border-bottom-width :border-right-width :border-bottom-right-radius]]]])

(defn on-barcode-read [opts data]
  (re-frame/dispatch [:qr-scanner.callback/scan-qr-code-success opts (camera/get-qr-code-data data)]))

(defview qr-scanner []
  (letsubs [read-once?        (atom false)
            {:keys [height width]} [:dimensions/window]
            camera-flashlight [:wallet.send/camera-flashlight]
            opts              [:get-screen-params]
            camera-ref        (atom nil)
            focus-object      (reagent/atom nil)
            layout            (atom nil)]
    (if config/qr-test-menu-enabled?
      [qr-test-view opts]
      [react/safe-area-view {:style {:flex             1
                                     :background-color colors/black-persist}}
       [topbar camera-flashlight opts]
       [react/with-activity-indicator
        {}
        [camera/camera
         {:ref                          #(reset! camera-ref %)
          :style                        {:flex 1}
          :capture-audio                false
          :on-layout                    (camera/on-layout layout)
          :auto-focus-point-of-interest @focus-object
          :on-tap                       (camera/on-tap camera-ref layout focus-object)
          :on-bar-code-read             #(when-not @read-once?
                                           (reset! read-once? true)
                                           (on-barcode-read opts %))}]]
       [viewfinder (int (* 2 (/ (min height width) 3)))]])))
