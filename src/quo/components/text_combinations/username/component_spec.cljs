(ns quo.components.text-combinations.username.component-spec
  (:require [quo.components.text-combinations.username.view :as username]
            [test-helpers.component :as h]))

(defn test-all-status
  [component-to-render component-props]
  (h/test "Verified status"
    (h/render [component-to-render (assoc component-props :status :verified)])
    (h/is-truthy (h/get-by-label-text :username-status-icon)))

  (h/test "Contact status"
    (h/render [component-to-render (assoc component-props :status :contact)])
    (h/is-truthy (h/get-by-label-text :username-status-icon)))

  (h/test "Untrustworthy status"
    (h/render [component-to-render (assoc component-props :status :untrustworthy)])
    (h/is-truthy (h/get-by-label-text :username-status-icon)))

  (h/test "Untrustworthy contact status"
    (h/render [component-to-render (assoc component-props :status :untrustworthy-contact)])
    (let [icons (h/get-all-by-label-text :username-status-icon)]
      (h/is-truthy (aget icons 0))
      (h/is-truthy (aget icons 1))))

  (h/test "Blocked status"
    (h/render [component-to-render (assoc component-props :status :blocked)])
    (h/is-truthy (h/get-by-label-text :username-status-icon))))

(h/describe "Text combinations - Username"
  (h/test "Renders default"
    (h/render [username/view {:username "Test username"}])
    (h/is-truthy (h/get-by-text "Test username")))

  (h/describe "Render different :name-type values"
    (h/describe "default"
      (let [props {:name-type :default
                   :username  "Test username"}]

        (h/test "default render"
          (h/render [username/view props])
          (h/is-truthy (h/get-by-text "Test username")))

        (h/describe "All status are rendered"
          (test-all-status username/view props))))

    (h/describe "ens"
      (let [props {:name-type :ens
                   :username  "test-username.eth"}]

        (h/test "no status render"
          (h/render [username/view props])
          (h/is-truthy (h/get-by-text "test-username.eth")))

        (h/describe "All status are rendered"
          (test-all-status username/view props))))

    (h/describe "nickname"
      (let [props {:name-type :nickname
                   :username  "Nickname"
                   :name      "Real name"}]

        (h/test "no status render"
          (h/render [username/view props])
          (h/is-truthy (h/get-by-text "Nickname"))
          (h/is-truthy (h/get-by-text "Real name")))

        (h/describe "All status are rendered"
          (test-all-status username/view props))))))
