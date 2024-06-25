(ns quo.components.wallet.missing-keypairs.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.icon :as icon]
    [quo.components.list-items.missing-keypair.view :as missing-keypair]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.missing-keypairs.style :as style]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn title-view
  [{:keys [keypairs blur? on-import-press]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:accessibility-label :title
      :style               style/title-container}
     [rn/view
      {:style style/title-icon-container}
      [icon/icon :i/info
       {:size  20
        :color colors/warning-60}]]
     [rn/view
      {:style style/title-info-container}
      [rn/view {:style style/title-row}
       [text/text
        {:weight :medium
         :style  {:color colors/warning-60}}
        (i18n/label :t/amount-missing-keypairs
                    {:amount (str (count keypairs))})]
       [button/button
        {:type       :outline
         :background :blur
         :size       24
         :on-press   on-import-press}
        (i18n/label :t/import)]]
      [text/text
       {:size  :paragraph-2
        :style (style/subtitle blur? theme)}
       (i18n/label :t/import-to-use-derived-accounts)]]]))

(defn- missing-keypair-item
  [keypair _index _separators
   {:keys [blur? on-options-press]}]
  [missing-keypair/view
   {:keypair          keypair
    :blur?            blur?
    :on-options-press on-options-press}])

(defn view
  [{:keys [blur? keypairs container-style on-options-press] :as props}]
  [rn/view
   {:style (merge style/container container-style)}
   [title-view props]
   [rn/flat-list
    {:data        keypairs
     :render-fn   missing-keypair-item
     :render-data {:blur?            blur?
                   :on-options-press on-options-press}
     :separator   [rn/view {:style {:height 8}}]}]])
