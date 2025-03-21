(ns status-im.contexts.keycard.nfc.sheets.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.nfc.sheets.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def duration 450)
(def timing-options #js {:duration duration})

(defn hide
  [translate-y bg-opacity window-height on-close]
  (when (fn? on-close)
    (on-close))
  ;; it will be better to use animation callback, but it doesn't work
  ;; so we have to use timeout, also we add 50ms for safety
  (js/setTimeout #(rf/dispatch [:keycard/connection-sheet-hidden]) (+ duration 250))
  ;; we want to give user some time (200ms) to see a success icon
  (js/setTimeout
   (fn []
     (reanimated/set-shared-value translate-y
                                  (reanimated/with-timing window-height timing-options))
     (reanimated/set-shared-value bg-opacity (reanimated/with-timing 0 timing-options)))
   200))

(defn show
  [translate-y bg-opacity]
  (reanimated/set-shared-value translate-y (reanimated/with-timing 0 timing-options))
  (reanimated/set-shared-value bg-opacity (reanimated/with-timing 1 timing-options)))

(defn android-view
  []
  (let [connected?                                    (rf/sub [:keycard/connected?])
        current-step                                  (rf/sub [:keycard/current-step])
        steps                                         (rf/sub [:keycard/steps])
        [was-connected-once? set-was-connected-once?] (rn/use-state false)
        {:keys [on-close theme hide? success?]}       (rf/sub [:keycard/connection-sheet-opts])
        theme                                         (or theme (quo.theme/use-theme))
        {window-height :height}                       (rn/get-window)
        bg-opacity                                    (reanimated/use-shared-value 0)
        translate-y                                   (reanimated/use-shared-value window-height)
        disconnected?                                 (and was-connected-once?
                                                           (not connected?)
                                                           (not success?))]
    (rn/use-effect #(if hide?
                      (hide translate-y bg-opacity window-height on-close)
                      (show translate-y bg-opacity))
                   [hide?])
    (rn/use-effect #(when connected? (set-was-connected-once? true) [connected?]))
    [rn/view {:style {:flex 1}}
     ;; backdrop
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity bg-opacity}
               {:flex             1
                :background-color colors/neutral-100-opa-70})}]
     ;; sheet
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:transform [{:translateY translate-y}]}
               (style/sheet theme))}
      (if success?
        [:<>
         [rn/image
          {:source (resources/get-image :nfc-success)
           :style  {:margin-top 64}}]
         [rn/text
          {:style {:font-size       16
                   :text-align      :center
                   :color           (colors/theme-colors colors/neutral-100 colors/white theme)
                   :margin-vertical 32}}
          (i18n/label :t/success)]]
        [:<>
         [rn/text
          {:style {:font-size     26
                   :color         (colors/theme-colors colors/neutral-100 colors/white theme)
                   :margin-bottom 32}}
          (if connected?
            (i18n/label :t/scanning)
            (if disconnected?
              (i18n/label :t/disconnected)
              (i18n/label :t/ready-to-scan)))]
         [rn/image
          {:source (resources/get-image (if disconnected? :nfc-fail :nfc-prompt-android))}]
         [rn/text
          {:style {:font-size       16
                   :text-align      :center
                   :color           (colors/theme-colors colors/neutral-100 colors/white theme)
                   :margin-vertical 32}}
          (if connected?
            (i18n/label :t/connected-dont-move)
            (i18n/label :t/hold-phone-near-keycard))
          (when steps
            (str "\n" (i18n/label :t/step-i-of-n {:step current-step :number steps})))]
         [rn/view {:style {:flex-direction :row}}
          [quo/button
           {:type                :dark-grey
            :container-style     {:flex 1}
            :accessibility-label :cancel-button
            :on-press            #(rf/dispatch [:keycard/hide-connection-sheet {:success? false}])}
           (i18n/label :t/cancel)]]])]]))
