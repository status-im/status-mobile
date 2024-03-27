(ns status-im.contexts.wallet.create-account.new-keypair.keypair-name.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.create-account.new-keypair.keypair-name.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def keypair-name-max-length 15)
(defn has-emojis? [s] (boolean (re-find utils.emojilib/emoji-regex s)))
(defn has-special-characters?
  [s]
  (if (empty? s)
    false
    (not (re-find #"^[a-zA-Z0-9\-_ ]+$" s))))


(defn view-internal
  [{:keys [theme]}]
  (let [keypair-name (reagent/atom "")]
    (fn []
      (let [customization-color (rf/sub [:profile/customization-color])
            [error? set-error?] (rn/use-state false)]
        [rn/view {:style {:flex 1}}
         [floating-button-page/view
          {:header [quo/page-nav
                    {:icon-name           :i/arrow-left
                     :on-press            #(rf/dispatch [:navigate-back])
                     :accessibility-label :top-bar}]
           :footer [quo/bottom-actions
                    {:actions          :one-action
                     :button-one-label (i18n/label :t/continue)
                     :button-one-props {:disabled?           (or (pos? error?)
                                                                 (< (count @keypair-name) 4))
                                        :customization-color customization-color
                                        :on-press            #(rf/dispatch [:wallet/new-keypair-continue
                                                                            {:keypair-name
                                                                             @keypair-name}])}
                     :container-style  style/bottom-action}]}
          [quo/text-combinations
           {:container-style style/header-container
            :title           (i18n/label :t/keypair-name)
            :description     (i18n/label :t/keypair-name-description)}]
          [quo/input
           {:container-style {:margin-horizontal 20}
            :placeholder     (i18n/label :t/keypair-name-input-placeholder)
            :label           (i18n/label :t/keypair-name)
            :char-limit      keypair-name-max-length
            :auto-focus      true
            :on-change-text  (fn [value]
                               (reset! keypair-name value)
                               (cond
                                 (> (count value) keypair-name-max-length) (set-error? 1)
                                 (has-emojis? value)                       (set-error? 2)
                                 (has-special-characters? value)           (set-error? 3)
                                 :else                                     (set-error? nil)))
            :error?          error?}]
          (when error?
            [rn/view
             {:style {:flex-direction  :row
                      :justify-content :center
                      :align-items     :center
                      :align-self      :flex-start
                      :margin-left     20
                      :margin-vertical 8}}
             [quo/icon :i/info {:color (colors/theme-colors colors/danger-50 colors/danger-60 theme)}]
             [quo/text
              {:style {:margin-left 4
                       :color       (colors/theme-colors colors/danger-50 colors/danger-60 theme)}}
              (i18n/label (keyword (str "t/key-name-error-" error?)))]])]]))))
(def view (quo.theme/with-theme view-internal))
