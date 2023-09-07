(ns quo2.components.settings.settings-item.component-spec
  (:require [quo2.components.settings.settings-item.view :as settings-item]
            [test-helpers.component :as h]))

(def props
  {:title               "Account"
   :accessibility-label :settings-item
   :action              :arrow
   :image               :icon
   :image-props         :i/browser})

(h/describe "Settings list tests"
  (h/test "Default render of Setting list component"
    (h/render [settings-item/view props])
    (h/is-truthy (h/get-by-label-text :settings-item)))

  (h/test "It renders a title"
    (h/render [settings-item/view props])
    (h/is-truthy (h/get-by-text "Account")))

  (h/test "its gets passed an on press event"
    (let [event (h/mock-fn)]
      (h/render [settings-item/view
                 (merge props {:on-press event})])
      (h/fire-event :press (h/get-by-text "Account"))
      (h/was-called event)))

  (h/test "on change event gets fired for toggle"
    (let [on-change (h/mock-fn)]
      (h/render [settings-item/view
                 (merge props
                        {:action       :selector
                         :action-props {:on-change on-change}})])
      (h/fire-event :press (h/get-by-label-text :toggle-off))
      (h/was-called on-change)))

  (h/test "It renders a label"
    (h/render [settings-item/view (merge props {:label :color})])
    (h/is-truthy (h/get-by-label-text :label-component)))

  (h/test "It renders a status tag component"
    (h/render [settings-item/view
               (merge props
                      {:tag       :context
                       :tag-props {:context "Test Tag"
                                   :icon    :i/placeholder}})])
    (h/is-truthy (h/get-by-text "Test Tag")))

  (h/test "on press event gets fired for button"
    (let [event (h/mock-fn)]
      (h/render [settings-item/view
                 (merge props
                        {:action       :button
                         :action-props {:button-text "test button"
                                        :on-press    event}})])
      (h/fire-event :press (h/get-by-text "test button"))
      (h/was-called event))))
