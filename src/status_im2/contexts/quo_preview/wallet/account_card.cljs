(ns status-im2.contexts.quo-preview.wallet.account-card
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.core :as quo]))

(def mock-data
  [{:id                  1
    :name                "Alisher account"
    :balance             "€2,269.12"
    :percentage-value    "16.9%"
    :amount              "€570.24"
    :customization-color :army
    :watch-only          false
    :type                :default
   }
   {:id               2
    :name             "Ben’s fortune"
    :balance          "€2,269.12"
    :percentage-value "16.9%"
    :amount           "€570.24"
    :watch-only       true
    :type             :watch-only
   }
   {:id      3
    :type    :add-account
    :handler #(js/alert "Add account pressed")
   }])

(defn- separator
  []
  [rn/view {:style {:width 40}}])

(defn cool-preview
  []
  [rn/view
   {:style {:margin-vertical 40
            :margin-left     40
            :flex            1}}
   [text/text {:size :heading-1 :weight :semi-bold :style {:margin-bottom 40}} "Account card"]
   [rn/flat-list
    {:data                           mock-data
     :keyExtractor                   #(str (:id %))
     :horizontal                     true
     :separator                      [separator]
     :render-fn                      quo/account-card
     :showsHorizontalScrollIndicator false
    }]])

(defn preview-account-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])