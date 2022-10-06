(ns status-im.utils.number)

(defn naive-round
  "Quickly and naively round number `n` up to `decimal-places`.

  Example usage: use it to avoid re-renders caused by floating-point number
  changes in Reagent atoms. Such numbers can be rounded up to a certain number
  of `decimal-places` in order to avoid re-rendering due to tiny fractional
  changes.

  Don't use this function for arbitrary-precision arithmetic."
  [n decimal-places]
  (let [scale (Math/pow 10 decimal-places)]
    (/ (Math/round (* n scale))
       scale)))
