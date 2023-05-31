(ns status-im2.contexts.syncing.scan-sync-code.view
  (:require ;[clojure.string :as string]
    [oops.core :as oops]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    ;[react-native.camera-kit :as camera-kit]
    [react-native.core :as rn]
    ;[react-native.blur :as blur]
    ;[react-native.hole-view :as hole-view]
    [react-native.permissions :as permissions]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.syncing.scan-sync-code.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    ;[status-im2.contexts.syncing.utils :as sync-utils]
    [react-native.reanimated :as reanimated]
    [status-im2.constants :as constants]))

(defonce camera-permission-granted? (reagent/atom false))

(defn- f-header
  [active-tab read-qr-once? title opacity]
  (let [subtitle-translate-y (reanimated/interpolate opacity [0 1] [-85 0])
        controls-translate-y (reanimated/interpolate opacity [0 1] [85 0])]
    [:<>
     [rn/view {:style style/header-container}
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:opacity   opacity
                 :transform [{:translateY controls-translate-y}]}
                {})}
       [quo/button
        {:icon                true
         :type                :blur-bg
         :size                32
         :accessibility-label :close-sign-in-by-syncing
         :override-theme      :dark
         :on-press            #(rf/dispatch [:navigate-back :sign-in])}
        :i/close]]
      [reanimated/view
       {:style (reanimated/apply-animations-to-style
                {:opacity   opacity
                 :transform [{:translateY controls-translate-y}]}
                {})}
       [quo/button
        {:before              :i/info
         :type                :blur-bg
         :size                32
         :accessibility-label :find-sync-code
         :override-theme      :dark
         :on-press            #(js/alert "Yet to be implemented")}
        (i18n/label :t/find-sync-code)]]]
     [quo/text
      {:size   :heading-1
       :weight :semi-bold
       :style  style/header-text}
      title]
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity   opacity
                :transform [{:translateY subtitle-translate-y}]}
               {})}
      [quo/text
       {:size   :paragraph-1
        :weight :regular
        :style  style/header-sub-text}
       (i18n/label :t/synchronise-your-data-across-your-devices)]]
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity   opacity
                :transform [{:translateY controls-translate-y}]}
               style/tabs-container)}
      [quo/segmented-control
       {:size           32
        :override-theme :dark
        :blur?          true
        :default-active @active-tab
        :data           [{:id 1 :label (i18n/label :t/scan-sync-qr-code)}
                         {:id 2 :label (i18n/label :t/enter-sync-code)}]
        :on-change      (fn [id]
                          (reset! active-tab id)
                          (reset! read-qr-once? false))}]]]))

(defn- header
  [active-tab read-qr-once? title subtitle-opacity]
  [:f> f-header active-tab read-qr-once? title subtitle-opacity])

(defn- camera-permission-view
  [request-camera-permission]
  [rn/view {:style style/camera-permission-container}
   [quo/text
    {:size   :paragraph-1
     :weight :medium
     :style  style/enable-camera-access-header}
    (i18n/label :t/enable-access-to-camera)]
   [quo/text
    {:size   :paragraph-2
     :weight :regular
     :style  style/enable-camera-access-sub-text}
    (i18n/label :t/to-scan-a-qr-enable-your-camera)]
   [quo/button
    {:before              :i/camera
     :type                :primary
     :size                32
     :accessibility-label :request-camera-permission
     :override-theme      :dark
     :on-press            request-camera-permission}
    (i18n/label :t/enable-camera)]])

(defn- qr-scan-hole-area
  [qr-view-finder]
  [rn/view
   {:style     style/qr-view-finder
    :on-layout (fn [event]
                 (let [layout      (js->clj (oops/oget event "nativeEvent.layout")
                                            :keywordize-keys
                                            true)
                       view-finder (assoc layout :height (:width layout))]
                   (reset! qr-view-finder view-finder)))}])

(defn- border
  [border1 border2 corner]
  [rn/view
   (assoc {:border-color colors/white :width 80 :height 80} border1 2 border2 2 corner 16)])

(defn- viewfinder
  [qr-view-finder]
  (let [size (:width qr-view-finder)]
    [:<>
     [rn/view {:style (style/viewfinder-container qr-view-finder)}

      [rn/view {:width size :height size :justify-content :space-between}
       [rn/view {:flex-direction :row :justify-content :space-between}
        [border :border-top-width :border-left-width :border-top-left-radius]
        [border :border-top-width :border-right-width :border-top-right-radius]]
       [rn/view {:flex-direction :row :justify-content :space-between}
        [border :border-bottom-width :border-left-width :border-bottom-left-radius]
        [border :border-bottom-width :border-right-width :border-bottom-right-radius]]]
      [quo/text
       {:size   :paragraph-2
        :weight :regular
        :style  style/viewfinder-text}
       (i18n/label :t/ensure-qr-code-is-in-focus-to-scan)]]]))

(defn- scan-qr-code-tab
  [qr-view-finder request-camera-permission]
  [:<>
   [rn/view {:style style/scan-qr-code-container}]
   (when (empty? @qr-view-finder)
     [qr-scan-hole-area qr-view-finder])
   (if (and @camera-permission-granted? (boolean (not-empty @qr-view-finder)))
     [viewfinder @qr-view-finder]
     [camera-permission-view request-camera-permission])])

(defn- enter-sync-code-tab
  []
  [rn/view {:style style/enter-sync-code-container}
   [quo/text
    {:size   :paragraph-1
     :weight :medium
     :style  {:color colors/white}}
    "Yet to be implemented"]])

(defn- bottom-view
  [insets]
  [rn/touchable-without-feedback
   {:on-press #(js/alert "Yet to be implemented")}
   [rn/view
    {:style (style/bottom-container (:bottom insets))}
    [quo/text
     {:size   :paragraph-2
      :weight :regular
      :style  style/bottom-text}
     (i18n/label :t/i-dont-have-status-on-another-device)]]])

#_(defn- check-qr-code-data
    [event]
    (let [connection-string        (string/trim (oops/oget event "nativeEvent.codeStringValue"))
          valid-connection-string? (sync-utils/valid-connection-string? connection-string)]
      (if valid-connection-string?
        (rf/dispatch [:syncing/input-connection-string-for-bootstrapping connection-string])
        (rf/dispatch [:toasts/upsert
                      {:icon           :i/info
                       :icon-color     colors/danger-50
                       :override-theme :light
                       :text           (i18n/label :t/error-this-is-not-a-sync-qr-code)}]))))

#_(defn render-camera
    [show-camera? qr-view-finder camera-ref on-read-code show-holes?]
    (when (and show-camera? (:x qr-view-finder))
      [:<>
       [rn/view {:style style/camera-container}
        [camera-kit/camera
         {:ref            #(reset! camera-ref %)
          :style          style/camera-style
          :camera-options {:zoomMode :off}
          :scan-barcode   true
          :on-read-code   on-read-code}]]
       [hole-view/hole-view
        {:style style/hole
         :holes (if show-holes?
                  [(merge qr-view-finder
                          {:borderRadius 16})]
                  [])}
        [blur/view
         {:style            style/absolute-fill
          :blur-amount      10
          :blur-type        :transparent
          :overlay-color    colors/neutral-80-opa-80
          :background-color colors/neutral-80-opa-80}]]]))

(defn f-view
  [{:keys [title show-bottom-view? #_background]}]
  (let [insets                    (safe-area/get-insets)
        active-tab                (reagent/atom 1)
        qr-view-finder            (reagent/atom {})
        request-camera-permission (fn []
                                    (rf/dispatch
                                     [:request-permissions
                                      {:permissions [:camera]
                                       :on-allowed  #(reset! camera-permission-granted? true)
                                       :on-denied   #(rf/dispatch
                                                      [:toasts/upsert
                                                       {:icon :i/info
                                                        :icon-color colors/danger-50
                                                        :override-theme :light
                                                        :text (i18n/label
                                                               :t/camera-permission-denied)}])}]))]
    (fn []
      (let [#_#_camera-ref                       (atom nil)
            read-qr-once?                    (atom false)
            ;; The below check is to prevent scanning of any QR code
            ;; when the user is in syncing progress screen
            #_#_user-in-syncing-progress-screen? (= (rf/sub [:view-id]) :syncing-progress)
            #_#_on-read-code                     (fn [data]
                                                   (when (and (not @read-qr-once?)
                                                              (not user-in-syncing-progress-screen?))
                                                     (reset! read-qr-once? true)
                                                     (js/setTimeout (fn []
                                                                      (reset! read-qr-once? false))
                                                                    3000)
                                                     (check-qr-code-data data)))
            #_#_scan-qr-code-tab?                (= @active-tab 1)
            #_#_show-camera?                     (and scan-qr-code-tab? @camera-permission-granted?)
            #_#_show-holes?                      (and show-camera?
                                                      (boolean (not-empty @qr-view-finder)))
            opacity         (reanimated/use-shared-value 0)
            tab-translate-y (reanimated/interpolate opacity [0 1] [85 0])]
        (rn/use-effect
         (fn []
           (reanimated/animate-shared-value-with-delay opacity
                                                       1 constants/onboarding-modal-animation-duration
                                                       :linear 0)
           (when-not @camera-permission-granted?
             (permissions/permission-granted? :camera
                                              #(reset! camera-permission-granted? %)
                                              #(reset! camera-permission-granted? false)))))
        [:<>
         #_[render-camera show-camera? @qr-view-finder camera-ref on-read-code show-holes?]
         [rn/view {:style (style/root-container (:top insets))}
          [header active-tab read-qr-once? title opacity]
          [reanimated/view
           {:style (reanimated/apply-animations-to-style
                    {:opacity   opacity
                     :transform [{:translateY tab-translate-y}]}
                    {})}
           (case @active-tab
             1 [scan-qr-code-tab qr-view-finder request-camera-permission]
             2 [enter-sync-code-tab]
             nil)]
          [rn/view {:style style/flex-spacer}]
          (when show-bottom-view? [bottom-view insets])]]))))

(defn view
  [props]
  [:f> f-view props])
