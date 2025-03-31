(ns status-im.contexts.syncing.setup-syncing.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [reagent.core :as reagent]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.common.resources :as resources]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.syncing.setup-syncing.style :as style]
    [status-im.contexts.syncing.utils :as sync-utils]
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
  (let [{:keys [customization-color]} (rf/sub [:profile/profile-with-image])
        valid-for-ms                  (reagent/atom code-valid-for-ms)
        code                          (reagent/atom nil)
        delay-ms                      (reagent/atom nil)
        timestamp                     (reagent/atom nil)
        set-code                      (fn [connection-string]
                                        (when (sync-utils/valid-connection-string? connection-string)
                                          (reset! timestamp (* 1000
                                                               (js/Math.ceil (/ (datetime/timestamp)
                                                                                1000))))
                                          (reset! delay-ms 1000)
                                          (reset! code connection-string)))
        clock                         (fn []
                                        (if (pos? (- code-valid-for-ms
                                                     (- (* 1000
                                                           (js/Math.ceil (/ (datetime/timestamp) 1000)))
                                                        @timestamp)))
                                          (swap! valid-for-ms (fn [_]
                                                                (- code-valid-for-ms
                                                                   (- (* 1000
                                                                         (js/Math.ceil
                                                                          (/ (datetime/timestamp) 1000)))
                                                                      @timestamp))))
                                          (reset! delay-ms nil)))
        cleanup-clock                 (fn []
                                        (reset! code nil)
                                        (reset! timestamp nil)
                                        (reset! valid-for-ms code-valid-for-ms))
        on-auth-success               (fn [entered-password]
                                        (rf/dispatch [:syncing/get-connection-string entered-password
                                                      set-code]))]
    (fn []
      [rn/view {:style (style/container-main)}
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
         [rn/view {:style style/qr-container}
          (if (sync-utils/valid-connection-string? @code)
            [qr-codes/qr-code {:url @code}]
            [rn/view {:style {:flex-direction :row}}
             [rn/image
              {:source (resources/get-image :qr-code)
               :style  {:width            "100%"
                        :background-color colors/white-opa-70
                        :border-radius    12
                        :aspect-ratio     1}}]])
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
                                                {:type :positive
                                                 :text (i18n/label
                                                        :t/sharing-copied-to-clipboard)}]))
               :type            :grey
               :container-style {:margin-top 12}
               :icon-left       :i/copy}
              (i18n/label :t/copy-qr)]])
          (when-not (sync-utils/valid-connection-string? @code)
            [rn/view {:style style/standard-auth}
             [standard-auth/slide-auth
              {:blur?                 true
               :size                  :size-40
               :track-text            (i18n/label :t/slide-to-reveal-code)
               :customization-color   customization-color
               :on-success            on-auth-success
               :auth-button-label     (i18n/label :t/reveal-sync-code)
               :auth-button-icon-left :i/reveal}]])]]
        [rn/view {:style style/sync-code}
         [quo/divider-label {:tight? false} (i18n/label :t/have-a-sync-code?)]
         [quo/action-drawer
          [[{:icon     :i/scan
             :on-press #(rf/dispatch [:open-modal :scan-sync-code-page])
             :label    (i18n/label :t/scan-or-enter-sync-code)}]]]]]])))
