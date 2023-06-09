(ns quo2.components.settings.category.component-spec
  (:require [quo2.components.settings.category-list.view :as category-list]
            [test-helpers.component :as h]))


(h/describe "Category List component"
            (h/test "renders category list component"
                    (h/render [category-list/category-list
                               {:title "Test Title"
                                :on-press #(println "Pressed")
                                :accessibility-label "Test Accessibility Label"
                                :left-icon nil
                                :chevron? true
                                :badge? true
                                :button-props {:title "Button" :on-press #(println "Button Pressed")}
                                :communities-props {:data [{:source "Community1"} {:source "Community2"}]}
                                :container-style {:margin 10}}])
                    (-> (js/expect (h/get-byLabelText "Test Accessibility Label"))
                        (.toBeTruthy)))

            (h/test "on press event fires"
                    (let [event (h/mock-fn)]
                      (h/render [category-list/category-list
                                 {:title "Test Title"
                                  :on-press event
                                  :accessibility-label "Test Accessibility Label"
                                  :left-icon nil
                                  :chevron? true
                                  :badge? true
                                  :button-props {:title "Button" :on-press #(println "Button Pressed")}
                                  :communities-props {:data [{:source "Community1"} {:source "Community2"}]}
                                  :container-style {:margin 10}}])
                      (h/fire-event :press (h/get-byLabelText "Test Accessibility Label"))
                      (-> (js/expect event)
                          (.toHaveBeenCalledTimes 1)))))
