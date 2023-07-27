(ns quo2.components.settings.settings-list.component-spec
  (:require [quo2.components.settings.settings-list.view :as settings-list]
            [test-helpers.component :as h]))

(h/describe "Settings list tests"
  (h/test "Default render of Setting list component"
    (h/render [settings-list/settings-list {:accessibility-label "test"}])
    (h/is-truthy (h/get-by-label-text :test)))

  (h/test "It renders a title"
    (h/render [settings-list/settings-list {:title "test"}])
    (h/is-truthy (h/get-by-text "test")))

  (h/test "its gets passed an on press event"
    (let [event (h/mock-fn)]
      (h/render [settings-list/settings-list
                 {:title    "test"
                  :on-press event}])
      (h/fire-event :press (h/get-by-text "test"))
      (h/was-called event)))

  (h/test "on change event gets fired for toggle"
    (let [on-change (h/mock-fn)]
      (h/render [settings-list/settings-list
                 {:title        "test"
                  :toggle-props {:on-change on-change}}])
      (h/fire-event :press (h/get-by-label-text :toggle-off))
      (h/was-called on-change)))

  (h/test "It renders a badge"
    (h/render [settings-list/settings-list {:badge? true}])
    (h/is-truthy (h/get-by-label-text :setting-list-badge)))

  (h/test "It renders a status tag component"
    (h/render [settings-list/settings-list
               {:status-tag-props
                {:size   :small
                 :status {:type :positive}
                 :label  "test tag"}}])
    (h/is-truthy (h/get-by-text "test tag")))

  (h/test "on press event gets fired for button"
    (let [event (h/mock-fn)]
      (h/render [settings-list/settings-list
                 {:button-props {:title    "test button"
                                 :on-press event}}])
      (h/fire-event :press (h/get-by-text "test button"))
      (h/was-called event)))

  (h/test "It renders a list of community icons"
    (h/render [settings-list/settings-list
               {:communities-props {:data
                                    [{:source              "1"
                                      :accessibility-label :community-1}
                                     {:source              "2"
                                      :accessibility-label :community-2}]}}])
    (h/is-truthy (h/get-by-label-text :community-1))
    (h/is-truthy (h/get-by-label-text :community-2))))
