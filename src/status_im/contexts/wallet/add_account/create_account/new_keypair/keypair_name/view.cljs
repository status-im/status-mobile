(ns status-im.contexts.wallet.add-account.create-account.new-keypair.keypair-name.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.validation.general :as validators]
    [status-im.contexts.wallet.add-account.create-account.new-keypair.keypair-name.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def keypair-name-max-length 15)
(def keypair-name-min-length 5)

(def error-messages
  {:length       (i18n/label :t/key-name-error-length)
   :emoji        (i18n/label :t/key-name-error-emoji)
   :special-char (i18n/label :t/key-name-error-special-char)})

(defn navigate-back [] (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [[keypair-name set-keypair-name] (rn/use-state "")
        customization-color             (rf/sub [:profile/customization-color])
        [error set-error]               (rn/use-state false)
        on-change-text                  (rn/use-callback (fn [value]
                                                           (set-keypair-name value)
                                                           (cond
                                                             (> (count value) keypair-name-max-length)
                                                             (set-error :length)
                                                             (validators/has-emojis? value) (set-error
                                                                                             :emoji)
                                                             (validators/has-special-characters? value)
                                                             (set-error :special-char)
                                                             :else (set-error nil))))
        on-continue                     (rn/use-callback #(rf/dispatch [:wallet/new-keypair-continue
                                                                        {:keypair-name
                                                                         keypair-name}])
                                                         [keypair-name])]
    [rn/view {:style {:flex 1}}
     [floating-button-page/view
      {:header [quo/page-nav
                {:icon-name           :i/arrow-left
                 :on-press            navigate-back
                 :accessibility-label :top-bar}]
       :footer [quo/bottom-actions
                {:actions          :one-action
                 :button-one-label (i18n/label :t/continue)
                 :button-one-props {:disabled?           (or (pos? error)
                                                             (< (count keypair-name)
                                                                keypair-name-min-length))
                                    :customization-color customization-color
                                    :on-press            on-continue}
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
        :on-change-text  on-change-text
        :error           error}]
      (when error
        [quo/info-message
         {:type            :error
          :size            :default
          :icon            :i/info
          :container-style style/error-container}
         (get error-messages error)])]]))
