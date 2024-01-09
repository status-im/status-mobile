(ns status-im.common.scan-qr-code.view
  (:require [clojure.string :as string]
            [oops.core :as oops]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.blur :as blur]
            [react-native.camera-kit :as camera-kit]
            [react-native.core :as rn]
            [react-native.hole-view :as hole-view]
            [react-native.permissions :as permissions]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im.common.device-permissions :as device-permissions]
            [status-im.common.scan-qr-code.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.transforms :as transforms]))

(defonce camera-permission-granted? (reagent/atom false))

(defn- header
  [{:keys [title subtitle]}]
  [:<>
   [rn/view {:style style/header-container}
    [quo/button
     {:icon-only?          true
      :type                :grey
      :background          :blur
      :size                32
      :accessibility-label :close-scan-qr-code
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/arrow-left]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  (style/header-text (when subtitle true))}
    title]
   (when subtitle
     [quo/text
      {:size   :paragraph-1
       :weight :regular
       :style  style/header-sub-text}
      subtitle])])

(defn get-labels-and-on-press-method
  []
  {:title-label-key       :t/enable-access-to-camera
   :description-label-key :t/to-scan-a-qr-enable-your-camera
   :button-icon           :i/camera
   :button-label          :t/enable-camera
   :accessibility-label   :request-camera-permission
   :on-press              (fn []
                            (device-permissions/camera #(reset! camera-permission-granted? true)))})

(defn- camera-permission-view
  []
  (let [{:keys [title-label-key
                description-label-key
                button-icon
                button-label
                accessibility-label
                on-press]} (get-labels-and-on-press-method)]
    [rn/view {:style style/camera-permission-container}
     [quo/text
      {:size   :paragraph-1
       :weight :medium
       :style  style/enable-camera-access-header}
      (i18n/label title-label-key)]
     [quo/text
      {:size   :paragraph-2
       :weight :regular
       :style  style/enable-camera-access-sub-text}
      (i18n/label description-label-key)]
     [quo/button
      {:icon-left           button-icon
       :type                :primary
       :size                32
       :accessibility-label accessibility-label
       :customization-color :blue
       :on-press            on-press}
      (i18n/label button-label)]]))

(defn- qr-scan-hole-area
  [qr-view-finder]
  [rn/view
   {:style     style/qr-view-finder
    :on-layout (fn [event]
                 (let [layout      (transforms/js->clj (oops/oget event "nativeEvent.layout"))
                       view-finder (assoc layout :height (:width layout))]
                   (reset! qr-view-finder view-finder)))}])

(defn- white-border
  [corner]
  (let [border-styles (style/white-border corner)]
    [rn/view
     [rn/view {:style (border-styles :border)}]
     [rn/view {:style (border-styles :tip-1)}]
     [rn/view {:style (border-styles :tip-2)}]]))

(defn- white-square
  [layout-size]
  [rn/view {:style (style/qr-view-finder-container layout-size)}
   [rn/view {:style style/view-finder-border-container}
    [white-border :top-left]
    [white-border :top-right]]
   [rn/view {:style style/view-finder-border-container}
    [white-border :bottom-left]
    [white-border :bottom-right]]])

(defn- viewfinder
  [qr-view-finder helper-text?]
  (let [layout-size (+ (:width qr-view-finder) 2)]
    [rn/view {:style (style/viewfinder-container qr-view-finder)}
     [white-square layout-size]
     (when helper-text?
       [quo/text
        {:size   :paragraph-2
         :weight :regular
         :style  style/viewfinder-text}
        (i18n/label :t/ensure-qr-code-is-in-focus-to-scan)])]))

(defn- scan-qr-code-tab
  [qr-view-finder helper-text?]
  (if (and @camera-permission-granted?
           (boolean (not-empty qr-view-finder)))
    [viewfinder qr-view-finder helper-text?]
    [camera-permission-view]))

(defn- check-qr-code-and-navigate
  [{:keys [event error-message validate-fn on-success-scan on-failed-scan scan-code?]}]
  (let [scanned-value (string/trim (oops/oget event "nativeEvent.codeStringValue"))
        validated?    (if validate-fn (validate-fn scanned-value) true)]
    ;; Note - camera-kit keeps scanning until the screen is dismissed,
    ;; so disable scanning to make sure the on-read-code doesn't get executed multiple times
    (reset! scan-code? false)
    (if validated?
      (on-success-scan scanned-value)
      (do
        (on-failed-scan)
        (debounce/debounce-and-dispatch
         [:toasts/upsert
          {:type  :negative
           :theme :dark
           :text  error-message}]
         300)))))

(defn- render-camera
  [{:keys [torch-mode qr-view-finder scan-code? validate-fn error-message set-qr-code-succeeded
           set-rescan-timeout]}]
  [:<>
   [rn/view {:style style/camera-container}
    [camera-kit/camera
     {:style        style/camera-style
      :camera-type  camera-kit/camera-type-back
      :zoom-mode    :off
      :torch-mode   torch-mode
      :scan-barcode true
      :on-read-code #(when @scan-code?
                       (check-qr-code-and-navigate {:event           %
                                                    :scan-code?      scan-code?
                                                    :validate-fn     validate-fn
                                                    :error-message   error-message
                                                    :on-success-scan set-qr-code-succeeded
                                                    :on-failed-scan  set-rescan-timeout}))}]]
   [hole-view/hole-view
    {:style style/hole
     :holes [(assoc qr-view-finder :borderRadius 16)]}
    [blur/view
     {:style            style/absolute-fill
      :blur-amount      10
      :blur-type        :transparent
      :overlay-color    colors/neutral-80-opa-80
      :background-color colors/neutral-80-opa-80}]]])

(defn- set-listener-torch-off-on-app-inactive
  [torch-atm]
  (let [set-torch-off-fn   #(when (not= % "active") (reset! torch-atm false))
        app-state-listener (.addEventListener rn/app-state "change" set-torch-off-fn)]
    #(.remove app-state-listener)))

(defn- navigate-back-handler
  []
  (rf/dispatch [:navigate-back])
  true)

(defn f-view-internal
  [{:keys [title subtitle validate-fn on-success-scan error-message]}]
  (let [insets             (safe-area/get-insets)
        qr-code-succeed?   (reagent/atom false)
        qr-view-finder     (reagent/atom {})
        torch?             (reagent/atom false)
        scan-code?         (reagent/atom true)
        set-rescan-timeout (fn []
                             (reset! scan-code? false)
                             (js/setTimeout #(reset! scan-code? true) 3000))]
    (fn []
      (let [torch-mode            (if @torch? :on :off)
            flashlight-icon       (if @torch? :i/flashlight-on :i/flashlight-off)
            show-camera?          (and @camera-permission-granted?
                                       (boolean (not-empty @qr-view-finder)))
            camera-ready-to-scan? (and show-camera?
                                       (not @qr-code-succeed?))]
        (rn/use-effect
         (fn []
           (rn/hw-back-add-listener navigate-back-handler)
           (set-listener-torch-off-on-app-inactive torch?)
           (when-not @camera-permission-granted?
             (permissions/permission-granted?
              :camera
              #(reset! camera-permission-granted? %)
              #(reset! camera-permission-granted? false)))
           #(rn/hw-back-remove-listener navigate-back-handler)))
        [:<>
         [rn/view {:style style/background}]
         (when camera-ready-to-scan?
           [render-camera
            {:torch-mode            torch-mode
             :qr-view-finder        @qr-view-finder
             :scan-code?            scan-code?
             :error-message         error-message
             :validate-fn           validate-fn
             :set-qr-code-succeeded (fn [value]
                                      (when on-success-scan
                                        (on-success-scan value))
                                      (rf/dispatch [:navigate-back]))
             :set-rescan-timeout    set-rescan-timeout}])
         [rn/view {:style (style/root-container (:top insets))}
          [header
           {:title    title
            :subtitle subtitle}]
          (when (empty? @qr-view-finder)
            [:<>
             [rn/view {:style style/scan-qr-code-container}]
             [qr-scan-hole-area qr-view-finder]])
          [scan-qr-code-tab @qr-view-finder (when subtitle true)]
          [rn/view {:style style/flex-spacer}]
          (when show-camera?
            [quo.theme/provider {:theme :light}
             [quo/button
              {:icon-only?          true
               :type                :grey
               :background          :photo
               :size                style/flash-button-size
               :accessibility-label :camera-flash
               :container-style     (style/camera-flash-button @qr-view-finder)
               :on-press            #(swap! torch? not)}
              flashlight-icon]])]]))))

(defn view-internal
  [props]
  [:f> f-view-internal props])

(def view (quo.theme/with-theme view-internal))
