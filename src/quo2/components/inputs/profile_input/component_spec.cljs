(ns quo2.components.inputs.profile-input.component-spec
  (:require [quo2.components.inputs.profile-input.view :as profile-input]
            [test-helpers.component :as h]))

(h/describe "Profile Input"
  (h/test "renders user avatar with placeholder name if no value is specified"
    (h/render [profile-input/profile-input
               {:placeholder "Your Name"}])
    (-> (js/expect (h/get-by-text "YN"))
        (.toBeTruthy)))

  (h/test "renders user avatar with full name if a value is specified"
    (let [event (h/mock-fn)]
      (h/render [profile-input/profile-input
                 {:on-change          event
                  :placeholder        "Your Name"
                  :image-picker-props {:full-name "Test Name"}}])
      (h/fire-event :change (h/get-by-text "TN"))
      (-> (js/expect (h/get-by-text "TN"))
          (.toBeTruthy))))

  (h/test "on press event fires"
    (let [event (h/mock-fn)]
      (h/render [profile-input/profile-input
                 {:placeholder "Your Name"
                  :on-press    event}])
      (h/fire-event :press (h/get-by-label-text :select-profile-picture-button))
      (-> (js/expect event)
          (.toHaveBeenCalledTimes 1)))))
