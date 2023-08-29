(ns quo2.components.wallet.wallet-activity.view
  (:require [quo2.theme :as quo.theme]
            [quo2.components.wallet.wallet-activity.style :as style]
            [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icon]
            [quo2.components.tags.context-tag.view :as context-tag]
            [react-native.hole-view :as hole-view]
            [utils.i18n :as i18n]
            [reagent.core :as reagent]))

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
   :finalised :i/diamond-blue
   :failed    :i/negative-state})

(defn transaction-header
  [{:keys [transaction
           timestamp
           counter
           theme
           blur?]
    :or   {transaction :receive
           counter     1}}]

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
       (str "x" counter "")]])
   [rn/view
    [text/text
     {:weight :regular
      :size   :label
      :style  (style/timestamp theme blur?)}
     timestamp]]])

(defn transaction-icon-view
  [{:keys [theme blur? transaction status]
    :or   {transaction :receive
           status      :pending}}]
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
  [_]
  (let [pressed? (reagent/atom false)]
    (fn
      [{:keys [state theme blur?
               on-press
               first second third fourth
               second-prefix third-prefix fourth-prefix]
        :as   props}]
      [rn/pressable
       {:style               (style/wallet-activity-container {:pressed? @pressed?
                                                               :theme    theme
                                                               :blur?    blur?})
        :accessibility-label :wallet-activity
        :disabled            (= state :disabled)
        :on-press            on-press
        :on-press-in         (fn [] (reset! pressed? true))
        :on-press-out        (fn [] (reset! pressed? false))}

       [rn/view
        {:style {:flex-direction :row}}
        [transaction-icon-view props]
        [rn/view ;content
         {:style style/content-container}

         [transaction-header props]

         [rn/view {:style style/content-line}
          (when first [prop-tag first blur?])
          (when second-prefix [prop-text second-prefix theme])
          (when second [prop-tag second blur?])]

         [rn/view {:style style/content-line}
          (when third-prefix [prop-text third-prefix theme])
          (when third [prop-tag third blur?])
          (when fourth-prefix [prop-text fourth-prefix theme])
          (when fourth [prop-tag fourth blur?])]]]])))

(def view
  "Properties:
        - :transaction - type of transaction`. Possible values:
          - :receive
          - :send
          - :swap
          - :bridge
          - :buy
          - :destroy
          - :mint
     
        - :status - transaction status. Possible values:
          - :pending
          - :confirmed
          - :finalised
          - :failed
     
        - :counter - amount of transactions shown by instance of the component
     
        - :timestamp - when transaction occured (string)
        - :blur?
        
        - :first - props for context tag component that will be first on the first line
        - :second - props for context tag component that will be second on the first line
        - :third - props for context tag component that will be first on the second line
        - :fourth - props for context tag component that will be second on the second line
     
        - :second-prefix - translation keyword to be used with label before second context tag
        - :third-prefix - translation keyword to be used with label before third context tag
        - :fourth-prefix - translation keyword to be used with label before fourth context tag
     
    "
  (quo.theme/with-theme view-internal))
