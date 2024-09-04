(ns quo.components.wallet.swap-input.view
  (:require [oops.core :as oops]
            [quo.components.avatars.token-avatar.view :as token-avatar]
            [quo.components.buttons.button.view :as buttons]
            [quo.components.dividers.divider-line.view :as divider-line]
            [quo.components.markdown.text :as text]
            [quo.components.tags.network-tags.view :as network-tag]
            [quo.components.wallet.approval-label.schema :as approval-label.schema]
            [quo.components.wallet.approval-label.view :as approval-label]
            [quo.components.wallet.swap-input.style :as style]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:type {:optional true} [:maybe [:enum :pay :receive]]]
      [:status {:optional true} [:maybe [:enum :default :typing :disabled :loading]]]
      [:token {:optional true} [:maybe :string]]
      [:value {:optional true} [:maybe :string]]
      [:default-value {:optional true} [:maybe :string]]
      [:currency-symbol {:optional true} [:maybe :string]]
      [:fiat-value {:optional true} [:maybe :string]]
      [:show-approval-label? {:optional true} [:maybe :boolean]]
      [:auto-focus? {:optional true} [:maybe :boolean]]
      [:input-disabled? {:optional true} [:maybe :boolean]]
      [:error? {:optional true} [:maybe :boolean]]
      [:show-keyboard? {:optional true} [:maybe :boolean]]
      [:approval-label-props {:optional true} [:maybe approval-label.schema/?schema]]
      [:network-tag-props {:optional true} [:maybe :map]]
      [:on-change-text {:optional true} [:maybe fn?]]
      [:enable-swap? {:optional true} [:maybe :boolean]]
      [:on-swap-press {:optional true} [:maybe fn?]]
      [:on-input-focus {:optional true} [:maybe fn?]]
      [:on-token-press {:optional true} [:maybe fn?]]
      [:on-max-press {:optional true} [:maybe fn?]]
      [:customization-color {:optional true} [:maybe :schema.common/customization-color]]
      [:container-style {:optional true} [:maybe :map]]]]]
   :any])

(defn view-internal
  [{:keys [type status token value fiat-value show-approval-label? error? network-tag-props
           approval-label-props default-value auto-focus? input-disabled? enable-swap?
           currency-symbol on-change-text show-keyboard?
           container-style on-swap-press on-token-press on-max-press on-input-focus]}]
  (let [theme             (quo.theme/use-theme)
        pay?              (= type :pay)
        disabled?         (= status :disabled)
        loading?          (= status :loading)
        typing?           (= status :typing)
        controlled-input? (some? value)
        input-ref         (rn/use-ref-atom nil)
        set-input-ref     (rn/use-callback (fn [ref] (reset! input-ref ref)) [])
        focus-input       (rn/use-callback (fn []
                                             (some-> @input-ref
                                                     (oops/ocall "focus")))
                                           [input-ref])]
    [rn/view
     {:style               container-style
      :accessibility-label :swap-input}
     [rn/view {:style (style/content typing? theme)}
      [rn/view
       {:style (style/row-1 loading?)}
       [rn/pressable {:on-press on-token-press}
        [token-avatar/view
         {:type  :asset
          :token token}]]
       (if loading?
         [rn/view {:style (style/row-1-loader theme)}]
         [:<>
          [rn/pressable
           {:style    style/input-container
            :on-press focus-input}
           [rn/text-input
            (cond-> {:ref                      set-input-ref
                     :style                    (style/input disabled? error? theme)
                     :placeholder-text-color   (colors/theme-colors colors/neutral-40
                                                                    colors/neutral-50
                                                                    theme)
                     :keyboard-type            :numeric
                     :editable                 (not input-disabled?)
                     :auto-focus               auto-focus?
                     :on-focus                 on-input-focus
                     :on-change-text           on-change-text
                     :show-soft-input-on-focus show-keyboard?
                     :default-value            default-value
                     :placeholder              "0"}
              controlled-input? (assoc :value value))]
           [text/text
            {:size   :paragraph-2
             :weight :semi-bold
             :style  (style/token-symbol theme)}
            token]]
          (when (and pay? enable-swap?)
            [buttons/button
             {:type       :outline
              :size       32
              :on-press   on-swap-press
              :icon-only? true}
             :i/reorder])])]
      [divider-line/view]
      [rn/view
       {:style (style/row-2 (or (not pay?) loading?))}
       (when-not loading?
         [:<>
          (when pay?
            [rn/pressable {:on-press on-max-press}
             [network-tag/view
              (assoc network-tag-props
                     :status
                     (if error? :error :default))]])
          (when fiat-value
            [text/text
             {:size   :paragraph-2
              :style  style/fiat-amount
              :weight :medium}
             (str currency-symbol fiat-value)])])
       (when loading?
         [rn/view {:style (style/row-2-loader theme)}])]]
     (when (and (not= status :loading) (= type :pay) show-approval-label?)
       [approval-label/view
        approval-label-props])]))

(def view (schema/instrument #'view-internal ?schema))
