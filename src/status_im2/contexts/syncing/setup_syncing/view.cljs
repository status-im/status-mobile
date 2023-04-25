(ns status-im2.contexts.syncing.setup-syncing.view
  (:require [utils.i18n :as i18n]
            [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.datetime :as datetime]
            [status-im2.contexts.syncing.setup-syncing.style :as style]
            [utils.re-frame :as rf]
            [status-im2.constants :as constants]
            [react-native.clipboard :as clipboard]
            [status-im2.contexts.syncing.sheets.enter-password.view :as enter-password]
            [status-im2.common.qr-code-viewer.view :as qr-code-viewer]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [react-native.hooks :as hooks]
            [react-native.safe-area :as safe-area]))

(def code-valid-for-ms 120000)
(def one-min-ms 60000)

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?            true
     :mid-section           {:type :text-only :main-text ""}
     :left-section          {:type                :grey
                             :icon                :i/close
                             :icon-override-theme :dark
                             :on-press            #(rf/dispatch [:navigate-back])}
     :right-section-buttons [{:type                :grey
                              :label               (i18n/label :t/how-to-scan)
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            #(js/alert "to be implemented")}]}]])

(defn valid-cs?
  [connection-string]
  (when connection-string
    (string/starts-with?
     connection-string
     constants/local-pairing-connection-string-identifier)))

(defn f-use-interval
  [clock cleanup-clock delay]
  (hooks/use-interval clock cleanup-clock delay))

(defn view
  []
  (let [valid-for-ms  (reagent/atom code-valid-for-ms)
        code          (reagent/atom nil)
        delay         (reagent/atom nil)
        timestamp     (reagent/atom nil)
        set-code      (fn [connection-string]
                        (when (valid-cs? connection-string)
                          (reset! timestamp (* 1000 (js/Math.ceil (/ (datetime/timestamp) 1000))))
                          (reset! delay 1000)
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
                          (reset! delay nil)))
        cleanup-clock (fn []
                        (reset! code nil)
                        (reset! timestamp nil)
                        (reset! valid-for-ms code-valid-for-ms))]

    (fn []
      [:f> f-use-interval clock cleanup-clock @delay]
      [rn/view {:style (style/container-main (safe-area/get-top))}
       [rn/scroll-view {}
        [navigation-bar]
        [rn/view {:style style/page-container}
         [rn/view {:style style/title-container}
          [quo/text
           {:size   :heading-1
            :weight :semi-bold
            :style  {:color colors/white}}
           (i18n/label :t/setup-syncing)]]
         [rn/view {:style (style/qr-container (valid-cs? @code))}
          (if (valid-cs? @code)
            [qr-code-viewer/qr-code-view 331 @code]
            [quo/qr-code
             {:source (resources/get-image :qr-code)
              :height 220
              :width  "100%"}])
          (when-not (valid-cs? @code)
            [quo/button
             {:on-press (fn []
                          ;TODO https://github.com/status-im/status-mobile/issues/15570
                          ;remove old bottom sheet when Authentication process design is created.
                          (rf/dispatch [:bottom-sheet/hide-old])
                          (rf/dispatch [:bottom-sheet/show-sheet-old
                                        {:content (fn []
                                                    [enter-password/sheet set-code])}]))
              :size     40
              :style    style/generate-button
              :before   :i/reveal} (i18n/label :t/reveal-sync-code)])
          (when (valid-cs? @code)
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
               :override-theme :dark
               :default-shown? true
               :editable       false}]
             [quo/button
              {:on-press       (fn []
                                 (clipboard/set-string @code)
                                 (rf/dispatch [:toasts/upsert
                                               {:icon       :correct
                                                :icon-color colors/success-50
                                                :text       (i18n/label
                                                             :t/sharing-copied-to-clipboard)}]))
               :override-theme :dark
               :type           :grey
               :style          {:margin-top 12}
               :before         :i/copy}
              (i18n/label :t/copy-qr)]])]]
        [rn/view {:style style/sync-code}
         [quo/divider-label
          {:label                 (i18n/label :t/have-a-sync-code?)
           :increase-padding-top? true}]
         [quo/action-drawer
          [[{:icon           :i/scan
             :override-theme :dark
             :on-press       #(js/alert "to be implemented")
             :label          (i18n/label :t/Scan-or-enter-sync-code)}]]]]]])))
