(ns quo2.components.wallet.transaction-summary.view
  (:require [quo2.theme :as quo.theme]
            [quo2.components.wallet.transaction-summary.style :as style]
            [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icon]
            [quo2.components.tags.context-tag.view :as context-tag]
            [utils.i18n :as i18n]))

(def transaction-translation
  {:send   (i18n/label :t/send)
   :swap   (i18n/label :t/swap)
   :bridge (i18n/label :t/bridge)})

(def transaction-icon
  {:send   :i/send
   :swap   :i/swap
   :bridge :i/bridge})

(defn transaction-header
  [{:keys [transaction
           theme]
    :or   {transaction :send}}]
  (let [icon        (transaction-icon transaction)
        translation (transaction transaction-translation)]
    [rn/view
     {:style style/transaction-header-container}
     [rn/view {:style style/icon-container}
      (when icon
        [icon/icon icon
         {:color               (style/icon-color theme)
          :accessibility-label :header-icon}])]
     (when translation
       [text/text
        {:weight :semi-bold
         :size   :paragraph-1
         :style  (style/transaction-header theme)}
        translation])]))

(defn prop-text
  [label theme]
  [text/text
   {:weight :regular
    :size   :paragraph-2
    :style  (style/prop-text theme)}
   (i18n/label label)])

(defn prop-tag
  [props]
  [rn/view {:style style/prop-tag}
   [context-tag/view (assoc props :size 24)]])

(defn extra-info
  [{:keys [header content theme]}]
  [rn/view {:style style/extra-info-container}
   [text/text
    {:weight :regular
     :size   :paragraph-2
     :style  (style/extra-info-header theme)}
    header]
   [text/text
    {:weight :medium
     :size   :paragraph-2
     :style  (style/extra-info-content theme)}
    content]])

(defn- view-internal
  [{:keys [theme first-tag second-tag third-tag fourth-tag second-tag-prefix
           third-tag-prefix fourth-tag-prefix fifth-tag max-fees
           nonce input-data]
    :as   props}]
  [rn/view
   {:style               (style/container theme)
    :accessibility-label :transaction-summary}
   [transaction-header props]
   [rn/view {:style style/content}
    [rn/view {:style style/content-line}
     (when first-tag [prop-tag first-tag])
     (when second-tag-prefix [prop-text second-tag-prefix theme])
     (when second-tag [prop-tag second-tag])]
    [rn/view {:style style/content-line}
     (when third-tag-prefix [prop-text third-tag-prefix theme])
     (when third-tag [prop-tag third-tag])
     (when fourth-tag-prefix [prop-text fourth-tag-prefix theme])
     (when fourth-tag [prop-tag fourth-tag])
     (when fifth-tag [prop-tag fifth-tag])]]
   [rn/view {:style (style/divider theme)}]
   [rn/view {:style style/extras-container}
    [extra-info
     {:header  (i18n/label :t/max-fees)
      :content max-fees
      :theme   theme}]
    [extra-info
     {:header  (i18n/label :t/nonce)
      :content nonce
      :theme   theme}]
    [extra-info
     {:header  (i18n/label :t/input-data)
      :content input-data
      :theme   theme}]]])

(def view
  "Properties:
        - :transaction - type of transaction`. Possible values:
          - :send
          - :swap
          - :bridge
             
        - :first-tag - props for context tag component that will be first on the first line
        - :second-tag - props for context tag component that will be second on the first line
        - :third-tag - props for context tag component that will be first on the second line
        - :fourth-tag - props for context tag component that will be second on the second line
        - :fifth-tag - props for context tag component that will be second on the second line
     
        - :second-tag-prefix - translation keyword to be used with label before second context tag
        - :third-tag-prefix - translation keyword to be used with label before third context tag
        - :fourth-tag-prefix - translation keyword to be used with label before fourth context tag
   
        - :max-fees - string
        - :nonce - digit
        - :input data - string
    "
  (quo.theme/with-theme view-internal))
