(ns quo2.components.wallet-user-avatar
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [clojure.string :as clojure-string]))

(def themes {:light {:primary   {:text-color       colors/primary-50
                                 :background-color colors/primary-20}
                     :purple {:text-color       colors/purple-opa-50
                              :background-color colors/purple-opa-20}
                     :indigo {:text-color       colors/indigo-opa-50
                              :background-color colors/indigo-opa-20}
                     :turquoise {:text-color       colors/turquoise-opa-50
                                 :background-color colors/turquoise-opa-20}
                     :blue {:text-color       colors/blue-opa-50
                            :background-color colors/blue-opa-20}
                     :green {:text-color       colors/green-opa-50
                             :background-color colors/green-opa-20}
                     :yellow {:text-color       colors/yellow-opa-50
                              :background-color colors/yellow-opa-20}
                     :orange {:text-color       colors/orange-opa-50
                              :background-color colors/orange-opa-20}
                     :red {:text-color       colors/red-opa-50
                           :background-color colors/red-opa-20}
                     :pink {:text-color       colors/pink-opa-50
                            :background-color colors/pink-opa-20}
                     :brown {:text-color       colors/brown-opa-50
                             :background-color colors/brown-opa-20}
                     :beige {:text-color       colors/beige-opa-50
                             :background-color colors/beige-opa-20}}
             :dark  {:primary   {:text-color       colors/primary-60
                                 :background-color colors/primary-20}
                     :purple {:text-color       colors/purple-opa-60
                              :background-color colors/purple-opa-20}
                     :indigo {:text-color       colors/indigo-opa-60
                              :background-color colors/indigo-opa-20}
                     :turquoise {:text-color       colors/turquoise-opa-60
                                 :background-color colors/turquoise-opa-20}
                     :blue {:text-color       colors/blue-opa-60
                            :background-color colors/blue-opa-20}
                     :green {:text-color       colors/green-opa-60
                             :background-color colors/green-opa-20}
                     :yellow {:text-color       colors/yellow-opa-60
                              :background-color colors/yellow-opa-20}
                     :orange {:text-color       colors/orange-opa-60
                              :background-color colors/orange-opa-20}
                     :red {:text-color       colors/red-opa-60
                           :background-color colors/red-opa-20}
                     :pink {:text-color       colors/pink-opa-60
                            :background-color colors/pink-opa-20}
                     :brown {:text-color       colors/brown-opa-60
                             :background-color colors/brown-opa-20}
                     :beige {:text-color       colors/beige-opa-60
                             :background-color colors/beige-opa-20}}})

(def circle-sizes {:small 20
                   :medium 32
                   :large 48
                   :x-large 80})

(def font-sizes {:x-large 27
                 :large 15
                 :medium 13
                 :small 11})

(defn wallet-user-avatar
  "params, first name, last name, color, size
   and if it's dark or not!"
  [{:keys [f-name l-name color dark? size] :or {f-name "John"
                                                l-name "Doe"
                                                color :red
                                                dark? false
                                                size :x-large}}]
  (let [circle-size (size circle-sizes)
        small? (= size :small)
        f-name-initial (-> f-name
                           clojure-string/upper-case
                           (subs 0 1))
        l-name-initial (-> l-name
                           clojure-string/upper-case
                           (subs 0 1))
        is-theme-dark? (if dark? :dark :light)
        circle-color (get-in themes [is-theme-dark? color :background-color])
        text-color (get-in themes [is-theme-dark? color :text-color])]
    [rn/view {:style {:width circle-size
                      :height circle-size
                      :border-radius circle-size
                      :text-align :center
                      :justify-content :center
                      :align-items :center
                      :background-color circle-color}}
     [rn/text {:style {:color text-color
                       :font-size (size font-sizes)}}
      (if small?
        (str f-name-initial)
        (str f-name-initial l-name-initial))]]))