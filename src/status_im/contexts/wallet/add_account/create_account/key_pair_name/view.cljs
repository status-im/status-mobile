(ns status-im.contexts.wallet.add-account.create-account.key-pair-name.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.common.validation.general :as validators]
    [status-im.contexts.wallet.add-account.create-account.key-pair-name.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private key-pair-name-max-length 15)
(def ^:private key-pair-name-min-length 5)

(def error-messages
  {:too-long     (i18n/label :t/key-name-error-length)
   :too-short    (i18n/label :t/key-name-error-too-short {:count key-pair-name-min-length})
   :emoji        (i18n/label :t/key-name-error-emoji)
   :special-char (i18n/label :t/key-name-error-special-char)})

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [workflow]}                (rf/sub [:get-screen-params])
        customization-color               (rf/sub [:profile/customization-color])
        [key-pair-name set-key-pair-name] (rn/use-state "")
        [error set-error]                 (rn/use-state nil)
        on-change-text                    (rn/use-callback
                                           (fn [value]
                                             (set-key-pair-name value)
                                             (cond
                                               (> (count value) key-pair-name-max-length)
                                               (set-error :too-long)

                                               (< 0 (count value) key-pair-name-min-length)
                                               (set-error :too-short)

                                               (validators/has-emojis? value)
                                               (set-error :emoji)

                                               (validators/has-special-characters? value)
                                               (set-error :special-char)

                                               :else (set-error nil))))
        on-continue                       (rn/use-callback
                                           (fn [_]
                                             (case workflow
                                               ;; TODO issue #19759. Implement creation account from
                                               ;; private key
                                               :import-private-key
                                               (not-implemented/alert)

                                               :new-key-pair
                                               (rf/dispatch [:wallet/new-keypair-continue
                                                             {:keypair-name key-pair-name}])
                                               (js/alert "Unknown workflow")))
                                           [workflow key-pair-name])
        disabled?                         (or (some? error) (string/blank? key-pair-name))]
    [rn/view {:style {:flex 1}}
     [floating-button-page/view
      {:customization-color customization-color
       :header              [quo/page-nav
                             {:icon-name           :i/arrow-left
                              :on-press            navigate-back
                              :accessibility-label :top-bar}]
       :footer              [quo/button
                             {:customization-color customization-color
                              :disabled?           disabled?
                              :on-press            on-continue}
                             (i18n/label :t/continue)]}
      [quo/page-top
       {:title            (i18n/label :t/keypair-name)
        :description-text (i18n/label :t/keypair-name-description)
        :description      :text
        :container-style  style/page-top}]
      [quo/input
       {:container-style style/input
        :placeholder     (i18n/label :t/keypair-name-input-placeholder)
        :label           (i18n/label :t/keypair-name)
        :char-limit      key-pair-name-max-length
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
