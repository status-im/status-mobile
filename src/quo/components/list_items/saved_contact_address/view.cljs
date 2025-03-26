(ns quo.components.list-items.saved-contact-address.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.saved-contact-address.schema :as component-schema]
    [quo.components.list-items.saved-contact-address.style :as style]
    [quo.components.markdown.text :as text]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [schema.core :as schema]
    [utils.address :as address]
    [utils.i18n :as i18n]))

(defn- account
  [{:keys [emoji name address customization-color]}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:accessibility-label :account-container
      :style               style/account-container}
     [account-avatar/view
      {:emoji               emoji
       :size                16
       :customization-color customization-color}]
     [text/text
      {:size   :paragraph-2
       :weight :monospace
       :style  (style/account-name theme)}
      name]
     [rn/view {:style (style/dot-divider theme)}]
     [text/text
      {:size   :paragraph-2
       :weight :monospace
       :style  (style/account-address theme)}
      (address/get-shortened-key address)]]))

(defn- internal-view
  [{:keys [contact-props accounts active-state? customization-color on-press]
    :or   {customization-color :blue
           accounts            []
           active-state?       true}}]
  (let [theme             (quo.context/use-theme)
        [state set-state] (rn/use-state :default)
        active?           (rn/use-ref-atom false)
        timer             (rn/use-ref-atom nil)
        on-press          (rn/use-callback #(when on-press (on-press)))
        on-press-in       (rn/use-callback
                           (fn []
                             (when-not (= state :selected)
                               (reset! timer (js/setTimeout #(set-state :pressed) 100))))
                           [state])
        accounts-count    (count accounts)
        account-props     (when (= accounts-count 1) (first accounts))
        on-press-out      (rn/use-callback
                           (fn []
                             (let [new-state (if (or (not active-state?) @active?) :default :active)]
                               (when @timer (js/clearTimeout @timer))
                               (reset! timer nil)
                               (reset! active? (= new-state :active))
                               (set-state new-state)))
                           [active-state?])]
    [rn/pressable
     {:style               (style/container {:state state :customization-color customization-color})
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :accessibility-label :container}
     [rn/view {:style style/left-container}
      [user-avatar/user-avatar (assoc contact-props :size :small)]
      [rn/view {:style style/saved-contact-container}
       [rn/view
        {:style style/account-title-container}
        [text/text
         {:weight :semi-bold
          :size   :paragraph-1}
         (:full-name contact-props)]
        [icon/icon :i/contact
         {:container-style     style/contact-icon-container
          :accessibility-label :contact-icon
          :size                12
          :color               (colors/theme-colors colors/primary-50 colors/primary-60 theme)
          :color-2             colors/white}]]
       (if account-props
         [account (assoc account-props :theme theme)]
         [text/text
          {:accessibility-label :accounts-count
           :size                :paragraph-2
           :style               (style/accounts-count theme)}
          (i18n/label :t/accounts-count {:count accounts-count})])]]
     (when (> accounts-count 1)
       [icon/icon :i/chevron-right
        {:accessibility-label :check-icon
         :size                20
         :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}])]))

(def view (schema/instrument #'internal-view component-schema/?schema))
