(ns quo2.components.notifications.notification.component-spec
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.components.notifications.notification.view :as notification]
    [test-helpers.component :as h]))

(h/describe "notification"
  (h/test "empty notification"
    (h/render [notification/notification {}])
    (h/is-null (h/query-by-label-text :notification-avatar))
    (h/is-null (h/query-by-label-text :notification-header))
    (h/is-null (h/query-by-label-text :notification-body)))
  (h/test "notification with title and text"
    (h/render [notification/notification
               {:title        "title"
                :title-weight :medium
                :text         "text"}])
    (-> (h/expect (h/get-by-label-text :notification-header))
        (.toHaveTextContent "title"))
    (-> (h/expect (h/get-by-label-text :notification-body))
        (.toHaveTextContent "text")))
  (h/test "notification with custom input"
    (h/render [notification/notification
               {:header [text/text {:accessibility-label :header} "custom header"]
                :avatar [text/text {:accessibility-label :avatar} "custom avatar"]
                :body   [text/text {:accessibility-label :body} "custom body"]}])
    (h/is-truthy (h/get-by-label-text :notification-avatar))
    (h/is-truthy (h/get-by-label-text :notification-header))
    (h/is-truthy (h/get-by-label-text :notification-body))
    (h/is-truthy (h/get-by-label-text :avatar))
    (h/is-truthy (h/get-by-label-text :header))
    (h/is-truthy (h/get-by-label-text :body))
    (-> (h/expect (h/get-by-label-text :notification-header))
        (.toHaveTextContent "custom header"))
    (-> (h/expect (h/get-by-label-text :notification-avatar))
        (.toHaveTextContent "custom avatar"))
    (-> (h/expect (h/get-by-label-text :notification-body))
        (.toHaveTextContent "custom body"))))
