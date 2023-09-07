(ns quo2.components.inputs.profile-input.component-spec
  (:require [quo2.components.inputs.profile-input.view :as profile-input]
            [test-helpers.component :as h]))

(h/describe "Profile Input"
  (h/test "on press event fires"
    (let [event (h/mock-fn)]
      (h/render [profile-input/profile-input
                 {:placeholder "Your Name"
                  :on-press    event}])
      (h/fire-event :press (h/get-by-label-text :select-profile-picture-button))
      (h/was-called-times event 1))))
