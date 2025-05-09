(ns quo.components.info.info-message.component-spec
  (:require
    [quo.components.info.info-message.view :as info-message]
    [test-helpers.component :as h]))

(h/describe "Info: Info Message"
  (h/test "basic render"
    (h/render-with-theme-provider
     [info-message/view
      {:status              :default
       :size                :default
       :accessibility-label :info-message
       :icon                :i/placeholder} "Message"])
    (h/is-truthy (h/get-by-label-text :info-message)))

  (h/test "render with correct message"
    (h/render-with-theme-provider
     [info-message/view
      {:status              :default
       :size                :default
       :accessibility-label :info-message
       :icon                :i/placeholder} "Message"])
    (h/is-truthy (h/get-by-text "Message")))

  (h/test "render icon"
    (h/render-with-theme-provider
     [info-message/view
      {:status              :default
       :size                :default
       :accessibility-label :info-message
       :icon                :i/placeholder} "Message"])
    (h/is-truthy (h/get-by-label-text :icon))))
