(ns quo.components.wallet.wallet-activity.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.tags.context-tag.view :as context-tag]
    [quo.components.wallet.wallet-activity.schema :as component-schema]
    [quo.components.wallet.wallet-activity.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.hole-view :as hole-view]
    [schema.core :as schema]
    [utils.i18n :as i18n]))

(def transaction-translation
  {:receive [i18n/label :t/receive]
   :send    [i18n/label :t/send]
   :swap    [i18n/label :t/swap]
   :bridge  [i18n/label :t/bridge]
   :buy     [i18n/label :t/buy]
   :destroy [i18n/label :t/destroy]
   :mint    [i18n/label :t/mint]})

(def transaction-icon
  {:receive :i/receive
   :send    :i/send
   :swap    :i/swap
   :bridge  :i/bridge
   :buy     :i/buy
   :destroy :i/destroy
   :mint    :i/mint})

(def status-icon
  {:pending   :i/pending-state
   :confirmed :i/positive-state
   :finalised :i/diamond
   :failed    :i/negative-state})

(defn transaction-header
  [{:keys [transaction
           timestamp
           counter
           blur?]
    :or   {transaction :receive
           counter     1}}
   theme]
  [rn/view
   {:style style/transaction-header-container}
   [text/text
    {:weight :semi-bold
     :size   :paragraph-1
     :style  (style/transaction-header theme)}
    (transaction transaction-translation)]
   (when (> counter 1)
     [rn/view (style/transaction-counter-container theme blur?)
      [text/text
       {:weight :medium
        :size   :label
        :style  (style/transaction-counter theme)}
       (i18n/label :t/x-counter {:counter counter})]])
   [rn/view {:style style/timestamp-container}
    [text/text
     {:weight :regular
      :size   :label
      :style  (style/timestamp theme blur?)}
     timestamp]]])

(defn transaction-icon-view
  [{:keys [blur? transaction status]
    :or   {transaction :receive
           status      :pending}}
   theme]
  [rn/view {:style style/icon-container}
   [hole-view/hole-view
    {:style (style/icon-hole-view theme blur?)
     :holes [{:x            20
              :y            20
              :right        0
              :width        12
              :height       12
              :borderRadius 6}]}
    [icon/icon (transaction-icon transaction)
     {:color (style/icon-color theme)}]]
   [rn/view {:style style/icon-status-container}
    [icon/icon (status-icon status)
     {:size     12
      :no-color :true}]]])

(defn prop-text
  [label theme]
  [text/text
   {:weight :regular
    :size   :paragraph-2
    :style  (style/prop-text theme)}
   [i18n/label label]])

(defn prop-tag
  [props blur?]
  [rn/view {:style {:margin-right 4}}
   [context-tag/view (merge props {:size 24 :blur? blur?})]])

(defn- view-internal
  [{:keys [state blur? first-tag second-tag third-tag fourth-tag on-press
           second-tag-prefix third-tag-prefix fourth-tag-prefix]
    :as   props}]
  (let [theme         (quo.theme/use-theme)
        [pressed?
         set-pressed] (rn/use-state false)
        on-press-in   (rn/use-callback #(set-pressed true))
        on-press-out  (rn/use-callback #(set-pressed false))]
    [rn/pressable
     {:style               (style/wallet-activity-container {:pressed? pressed?
                                                             :theme    theme
                                                             :blur?    blur?})
      :accessibility-label :wallet-activity
      :disabled            (= state :disabled)
      :on-press            on-press
      :on-press-in         on-press-in
      :on-press-out        on-press-out}
     [rn/view
      {:style {:flex-direction :row}}
      [transaction-icon-view props theme]
      [rn/view
       {:style style/content-container}
       [transaction-header props theme]
       [rn/view {:style style/content-line}
        (when first-tag [prop-tag first-tag blur?])
        (when second-tag-prefix [prop-text second-tag-prefix theme])
        (when second-tag [prop-tag second-tag blur?])]
       [rn/view {:style style/content-line}
        (when third-tag-prefix [prop-text third-tag-prefix theme])
        (when third-tag [prop-tag third-tag blur?])
        (when fourth-tag-prefix [prop-text fourth-tag-prefix theme])
        (when fourth-tag [prop-tag fourth-tag blur?])]]]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
