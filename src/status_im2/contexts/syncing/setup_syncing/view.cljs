(ns status-im2.contexts.syncing.setup-syncing.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [reagent.core :as reagent]
            [status-im2.common.qr-code-viewer.view :as qr-code-viewer]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.syncing.setup-syncing.style :as style]
            [status-im2.contexts.syncing.sheets.enter-password.view :as enter-password]
            [status-im2.contexts.syncing.utils :as sync-utils]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def code-valid-for-ms 120000)
(def one-min-ms 60000)

(defn f-use-interval
  [clock cleanup-clock delay-ms]
  (hooks/use-interval clock cleanup-clock delay-ms)
  nil)

(defn view
  []
  (let [profile-color (rf/sub [:profile/customization-color])
        valid-for-ms  (reagent/atom code-valid-for-ms)
        code          (reagent/atom nil)
        delay-ms      (reagent/atom nil)
        timestamp     (reagent/atom nil)
        set-code      (fn [connection-string]
                        (when (sync-utils/valid-connection-string? connection-string)
                          (reset! timestamp (* 1000 (js/Math.ceil (/ (datetime/timestamp) 1000))))
                          (reset! delay-ms 1000)
                          (reset! code connection-string)))
        clock         (fn []
                        (if (pos? (- code-valid-for-ms
                                     (- (* 1000 (js/Math.ceil (/ (datetime/timestamp) 1000)))
                                        @timestamp)))
                          (swap! valid-for-ms (fn [_]
                                                (- code-valid-for-ms
                                                   (- (* 1000
                                                         (js/Math.ceil (/ (datetime/timestamp) 1000)))
                                                      @timestamp))))
                          (reset! delay-ms nil)))
        cleanup-clock (fn []
                        (reset! code nil)
                        (reset! timestamp nil)
                        (reset! valid-for-ms code-valid-for-ms))]

    (fn []
      [rn/view {:style style/container-main}
       [:f> f-use-interval clock cleanup-clock @delay-ms]
       [rn/scroll-view {}
        [quo/page-nav
         {:type       :no-title
          :icon-name  :i/close
          :background :blur
          :on-press   #(rf/dispatch [:navigate-back])
          :right-side [{:icon-left :i/info
                        :label     (i18n/label :t/how-to-pair)
                        :on-press  #(rf/dispatch [:open-modal :how-to-pair])}]}]
        [rn/view {:style style/page-container}
         [rn/view {:style style/title-container}
          [quo/text
           {:size   :heading-1
            :weight :semi-bold
            :style  {:color colors/white}}
           (i18n/label :t/setup-syncing)]]
         [rn/view {:style (style/qr-container (sync-utils/valid-connection-string? @code))}
          (if (sync-utils/valid-connection-string? @code)
            [qr-code-viewer/qr-code-view 331 @code]
            [quo/qr-code
             {:source (resources/get-image :qr-code)
              :height 220
              :width  "100%"}])
          (when-not (sync-utils/valid-connection-string? @code)
            [quo/button
             {:on-press            (fn []
                                     ;TODO https://github.com/status-im/status-mobile/issues/15570
                                     ;remove old bottom sheet when Authentication process design is
                                     ;created.
                                     (rf/dispatch [:bottom-sheet/hide-old])
                                     (rf/dispatch [:bottom-sheet/show-sheet-old
                                                   {:content (fn []
                                                               [enter-password/sheet set-code])}]))
              :type                :primary
              :customization-color profile-color
              :size                40
              :container-style     style/generate-button
              :icon-left           :i/reveal}
             (i18n/label :t/reveal-sync-code)])
          (when (sync-utils/valid-connection-string? @code)
            [rn/view
             {:style style/valid-cs-container}
             [rn/view
              {:style style/sub-text-container}
              [quo/text
               {:size  :paragraph-2
                :style {:color colors/white-opa-40}}
               (i18n/label :t/sync-code)]
              [quo/text
               {:size  :paragraph-2
                :style {:color (if (< @valid-for-ms one-min-ms)
                                 colors/danger-60
                                 colors/white-opa-40)}}
               (i18n/label :t/valid-for-time {:valid-for (datetime/ms-to-duration @valid-for-ms)})]]
             [quo/input
              {:default-value  @code
               :type           :password
               :default-shown? true
               :editable       false}]
             [quo/button
              {:on-press        (fn []
                                  (clipboard/set-string @code)
                                  (rf/dispatch [:toasts/upsert
                                                {:icon       :correct
                                                 :icon-color colors/success-50
                                                 :text       (i18n/label
                                                              :t/sharing-copied-to-clipboard)}]))
               :type            :grey
               :container-style {:margin-top 12}
               :icon-left       :i/copy}
              (i18n/label :t/copy-qr)]])]]
        [rn/view {:style style/sync-code}
         [quo/divider-label {:tight? false} (i18n/label :t/have-a-sync-code?)]
         [quo/action-drawer
          [[{:icon     :i/scan
             :on-press #(rf/dispatch [:navigate-to :scan-sync-code-page])
             :label    (i18n/label :t/scan-or-enter-sync-code)}]]]]]])))
