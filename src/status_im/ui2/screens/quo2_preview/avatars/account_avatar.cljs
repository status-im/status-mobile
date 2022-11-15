(ns status-im.ui2.screens.quo2-preview.avatars.account-avatar
  (:require [reagent.core :as reagent]
            [react-native.core :as rn]
            [status-im.ui2.screens.quo2-preview.preview :as preview]
            [quo2.components.avatars.account-avatar :as quo2]))

(def descriptor [{:label   "Icon"
                  :key     :icon
                  :type    :select
                  :options [{:key   :main-icons/wallet
                             :value "Wallet"}
                            {:key   :main-icons/token
                             :value "Token"}
                            {:key   :main-icons/status
                             :value "Status"}]}
                 {:label   "Size"
                  :key     :size
                  :type    :select
                  :options [{:key   20
                             :value "Small"}
                            {:key   24
                             :value "Medium"}
                            {:key   32
                             :value "Big"}
                            {:key   48
                             :value "Very big"}
                            {:key   80
                             :value "Seriously Big!"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:size 80
                             :icon :main-icons/wallet})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/account-avatar @state]]])))

(defn preview-account-avatar []
  [rn/view {:flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
