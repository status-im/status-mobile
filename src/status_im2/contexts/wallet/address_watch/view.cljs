(ns status-im2.contexts.wallet.address-watch.view
  (:require
    [clojure.string :as string]
    [quo2.core :as quo]
    [quo2.theme :as quo.theme]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.address-watch.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view-internal
  []
  (let [top         (safe-area/get-top)
        bottom      (safe-area/get-bottom)
        input-value (reagent/atom "")]
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
          :on-change       #(reset! input-value %)
          :default-value   @input-value}]
        [quo/button
         {:icon-only? true
          :type       :outline} :i/scan]]
       [rn/view {:style (style/button-container bottom)}
        [quo/text "[WIP] Bottom Actions"]]])))

(def view (quo.theme/with-theme view-internal))
