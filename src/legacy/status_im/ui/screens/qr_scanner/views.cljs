(ns legacy.status-im.ui.screens.qr-scanner.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.qr-scanner.styles :as styles]
    [re-frame.core :as re-frame]
    [react-native.camera-kit :as camera-kit]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.config :as config]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn get-qr-code-data
  [^js event]
  (when-let [data (-> event .-nativeEvent .-codeStringValue)]
    (string/trim data)))

(defn- topbar
  [_ {:keys [title] :as opts}]
  [topbar/topbar
   {:background      colors/black-persist
    :use-insets      true
    :border-bottom   false
    :navigation      :none
    :left-component  [quo/button
                      {:type     :secondary
                       :on-press #(re-frame/dispatch [:qr-scanner.callback/scan-qr-code-cancel opts])}
                      [quo/text {:style {:color colors/white-persist}}
                       (i18n/label :t/cancel)]]
    :title-component [quo/text
                      {:style           {:color colors/white-persist}
                       :weight          :bold
                       :number-of-lines 1
                       :align           :center
                       :size            :large}
                      (or title (i18n/label :t/scan-qr-code))]}])

(defn qr-test-view
  [opts]
  (let [text-value (atom "")]
    [react/view
     {:flex            1
      :align-items     :center
      :justify-content :center}
     [react/text-input
      {:multiline      true
       :style          {:color colors/white-persist}
       :on-change-text #(reset! text-value %)}]
     [react/view {:flex-direction :row}
      [quo/button
       {:on-press #(re-frame/dispatch [:qr-scanner.callback/scan-qr-code-cancel opts])}
       "Cancel"]
      [quo/button
       {:on-press #(re-frame/dispatch
                    [:qr-scanner.callback/scan-qr-code-success opts
                     (when-let [text @text-value]
                       (-> text string/trim (string/replace #"^Ethereum:" "ethereum:")))])}
       "Ok"]]]))

(defn corner
  [border1 border2 corner-radius]
  [react/view
   {:style (assoc {:border-color colors/white-persist :width 60 :height 60}
                  border1
                  5
                  border2
                  5
                  corner-radius
                  32)}])

(defn- viewfinder
  [size]
  [react/view {:style styles/viewfinder-port}
   [react/view {:width size :height size :justify-content :space-between}
    [react/view {:flex-direction :row :justify-content :space-between}
     [corner :border-top-width :border-left-width :border-top-left-radius]
     [corner :border-top-width :border-right-width :border-top-right-radius]]
    [react/view {:flex-direction :row :justify-content :space-between}
     [corner :border-bottom-width :border-left-width :border-bottom-left-radius]
     [corner :border-bottom-width :border-right-width :border-bottom-right-radius]]]])

(defn on-barcode-read
  [opts data]
  (re-frame/dispatch [:qr-scanner.callback/scan-qr-code-success opts (get-qr-code-data data)]))

(defn- navigate-back-handler
  []
  (re-frame/dispatch [:navigate-back])
  true)

(defn f-qr-scanner
  []
  (let [read-once?             (reagent/atom false)
        {:keys [height width]} (rf/sub [:dimensions/window])
        camera-flashlight      (rf/sub [:wallet-legacy.send/camera-flashlight])
        opts                   (rf/sub [:get-screen-params])
        camera-ref             (reagent/atom nil)]
    [react/view
     {:flex             1
      :background-color colors/black-persist}
     [topbar camera-flashlight opts]
     (if config/qr-test-menu-enabled?
       [qr-test-view opts]
       [react/view {:flex 1}
        [react/with-activity-indicator
         {}
         [camera-kit/camera
          {:ref          #(reset! camera-ref %)
           :style        {:flex 1}
           :camera-type  camera-kit/camera-type-back
           :zoom-mode    :off
           :scan-barcode true
           :on-read-code #(when-not @read-once?
                            (reset! read-once? true)
                            (on-barcode-read opts %))}]]
        [viewfinder (int (* 2 (/ (min height width) 3)))]])]))

(defn qr-scanner
  []
  (reagent/create-class
   {:display-name           "qr-scanner"
    :component-did-mount    #(rn/hw-back-add-listener navigate-back-handler)
    :component-will-unmount #(rn/hw-back-remove-listener navigate-back-handler)
    :reagent-render         f-qr-scanner}))
