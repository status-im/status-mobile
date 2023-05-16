(ns quo2.components.keycard.component-spec
  (:require [quo2.components.keycard.view :as keycard]
            [test-helpers.component :as h]
            [utils.i18n :as i18n]))

(h/describe "keycard component"
  (h/test "Render of keycard component when: Status = Empty, Locked = False"
    (h/render [keycard/keycard])
    (-> (h/expect (h/query-by-label-text :holder-name))
        (h/is-equal (i18n/label :t/empty-keycard))))

  (h/test "Render of keycard component when: Status = Filled, Locked = False"
    (h/render [keycard/keycard {:holder-name? "Alisha"}])
    (-> (h/expect (h/query-by-label-text :holder-name))
        (h/is-equal (i18n/label :t/user-keycard {:name :holder-name}))))

  (h/test "Render of keycard component when: Status = Filled, Locked = True"
    (h/render [keycard/keycard {:holder-name? "Alisha"}])
    (-> (h/expect (h/query-by-label-text :holder-name))
        (h/is-equal (i18n/label :t/user-keycard {:name :holder-name})))))
