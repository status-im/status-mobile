(ns quo2.components.wallet.account-card.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icon]
            [quo2.components.wallet.account-card.style :as style]
            [quo2.components.buttons.button.view :as button]
            [utils.i18n :as i18n]
            [quo2.components.markdown.text :as text] 
            [quo2.theme :as theme]))

(defn user-account
  [{:keys [name balance percentage-value amount customization-color type emoji theme]}]
  (let [watch-only? (= :watch-only type)]
    [:<>
     [rn/view (style/card customization-color watch-only? theme)
      [rn/view style/profile-container
       [rn/view {:style {:padding-bottom 2 :margin-right 2}}
        [text/text {:style style/emoji} emoji]]
       [rn/view {:style style/watch-only-container}
        [text/text
         {:size   :paragraph-2
          :weight :medium
          :style  (style/account-name watch-only? theme)}
         name]
        (when watch-only? [icon/icon :reveal {:color colors/neutral-50 :size 12}])]]

      [text/text
       {:size   :heading-2
        :weight :semi-bold
        :style  (style/account-value watch-only? theme)} 
        balance]
      [rn/view style/metrics-container
       [rn/view {:style {:margin-right 4}}
        [icon/icon :positive
         {:color (if (and watch-only? (not (colors/dark?)))
                   colors/neutral-50
                   colors/white)
          :size  16}]]
       [text/text
        {:weight :semi-bold
         :size   :paragraph-2
         :style  (style/metrics watch-only?)} 
         percentage-value]
       [rn/view (style/separator watch-only?)]
       [text/text
        {:weight :semi-bold
         :size   :paragraph-2
         :style  (style/metrics watch-only?)} amount]]]]))

(defn- add-account-view
  [{:keys [handler customization-color]}]
  [rn/view (style/add-account-container)
   [button/button
    {:type                :primary
     :size                32
     :icon                true
     :accessibility-label :add-account
     :on-press            handler
     :customization-color customization-color}
    :i/add]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:margin-top 4}} 
     (i18n/label :t/add-account)]])

(defn- view-internal
  [{:keys [type] :as props}]
  (case type
    :watch-only  [user-account props]
    :add-account [add-account-view props]
    :default     [user-account props]
    nil))

(def view (theme/with-theme view-internal))
