(ns quo2.components.avatar.avatar-themes 
  (:require [quo2.foundations.colors :as colors]))

(def themes {:light {:primary   {:icon-color       colors/primary-50
                                 :background-color colors/primary-20}
                     :purple {:icon-color       colors/purple-50
                              :background-color colors/purple-20}
                     :indigo {:icon-color       colors/indigo-50
                              :background-color colors/indigo-20}
                     :turquoise {:icon-color       colors/turquoise-50
                                 :background-color colors/turquoise-20}
                     :blue {:icon-color       colors/blue-50
                            :background-color colors/blue-20}
                     :green {:icon-color       colors/green-50
                             :background-color colors/green-20}
                     :yellow {:icon-color       colors/yellow-50
                              :background-color colors/yellow-20}
                     :orange {:icon-color       colors/orange-50
                              :background-color colors/orange-20}
                     :red {:icon-color       colors/red-50
                           :background-color colors/red-20}
                     :pink {:icon-color       colors/pink-50
                            :background-color colors/pink-20}
                     :brown {:icon-color       colors/brown-50
                             :background-color colors/brown-20}
                     :beige {:icon-color       colors/beige-50
                             :background-color colors/beige-20}}
             :dark  {:primary   {:icon-color       colors/primary-60
                                 :background-color colors/primary-50-opa-20}
                     :purple {:icon-color       colors/purple-60
                              :background-color colors/purple-20}
                     :indigo {:icon-color       colors/indigo-60
                              :background-color colors/indigo-20}
                     :turquoise {:icon-color       colors/turquoise-60
                                 :background-color colors/turquoise-20}
                     :blue {:icon-color       colors/blue-60
                            :background-color colors/blue-20}
                     :green {:icon-color       colors/green-60
                             :background-color colors/green-20}
                     :yellow {:icon-color       colors/yellow-60
                              :background-color colors/yellow-20}
                     :orange {:icon-color       colors/orange-60
                              :background-color colors/orange-20}
                     :red {:icon-color       colors/red-60
                           :background-color colors/red-20}
                     :pink {:icon-color       colors/pink-60
                            :background-color colors/pink-20}
                     :brown {:icon-color       colors/brown-60
                             :background-color colors/brown-20}
                     :beige {:icon-color       colors/beige-60
                             :background-color colors/beige-20}}})