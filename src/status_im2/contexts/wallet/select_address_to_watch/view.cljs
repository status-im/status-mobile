(ns status-im2.contexts.wallet.select-address-to-watch.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [re-frame.core :as re-frame]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.select-address-to-watch.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view-internal
  []
  (let [top                 (safe-area/get-top)
        bottom              (safe-area/get-bottom)
        input-value         (reagent/atom "")
        customization-color (rf/sub [:profile/customization-color])]
    (fn []
      [rn/view
       {:style {:flex       1
                :margin-top top}}
       [quo/page-nav
        {:type      :no-title
         :icon-name :i/close
         :on-press  #(rf/dispatch [:navigate-back])}]
       [quo/text-combinations
        {:container-style style/header-container
         :title           (i18n/label :t/add-address)
         :description     (i18n/label :t/enter-eth)}]
       [rn/view {:style style/input-container}
        [quo/input
         {:label           (i18n/label :t/eth-or-ens)
          :button          {:on-press (fn [] (clipboard/get-string #(reset! input-value %)))
                            :text     (i18n/label :t/paste)}
          :placeholder     (str "0x123abc... " (string/lower-case (i18n/label :t/or)) " bob.eth")
          :container-style {:margin-right 12
                            :flex         1}
          :weight          :monospace
          :on-change-text  #(reset! input-value %)
          :default-value   @input-value}]
        [quo/button
         {:icon-only? true
          :type       :outline} :i/scan]]
       [quo/button
        {:customization-color customization-color
         :disabled?           (clojure.string/blank? @input-value)
         :on-press            #(re-frame/dispatch [:navigate-to
                                                   :address-to-watch-edit
                                                   {:address @input-value}])
         :container-style     (style/button-container bottom)}
        (i18n/label :t/continue)]])))

(def view (quo.theme/with-theme view-internal))
