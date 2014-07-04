/*
 * This is free and unencumbered software released into the public domain.
 *
 * Please see https://github.com/binkley/binkley/blob/master/LICENSE.md.
 */

package hm.binkley.util;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.text.NumberFormat.getCurrencyInstance;

/**
 * {@code Money} <b>needs documentation</b>.
 *
 * @author <a href="mailto:binkley@alumni.rice.edu">B. K. Oxley (binkley)</a>
 * @todo Needs documentation.
 * @todo Help with formatting, perhaps format()?
 * @todo How to handle currencies with more than one locale, e.g., EUR?
 */
public final class Money
        implements Comparable<Money> {
    /** @todo Does JDK have a pattern for floating point? */
    private static final Pattern ISO = Pattern.compile("^([A-Z]{3})\\s*(\\S+)$");
    private final Currency currency;
    private final BigDecimal amount;

    /**
     * Creates a new {@code Money} instance for the given text <var>value</var> with a leading
     * currency followed by an amount.
     *
     * @param value the text representation of money, never missing
     *
     * @return the money instance, never missing
     *
     * @throws MoneyFormatException if <var>value</var> is not well-formed
     * @todo Are Special Drawing Rights, et al, handled correctly?
     * @todo Negative amounts: accept surrounding parens or only minus sign?
     * @todo Some kind of caching?
     * @todo Needs a rounding mode!
     */
    public static Money parse(@Nonnull final String value)
            throws MoneyFormatException {
        try {
            final Matcher matcher = ISO.matcher(value);
            if (matcher.matches()) {
                final Currency currency = Currency.getInstance(matcher.group(1));
                final BigDecimal amount = new BigDecimal(matcher.group(2));
                final int digits = currency.getDefaultFractionDigits();
                // SDR returns -1
                return new Money(currency, 0 > digits ? amount : amount.setScale(digits));
            }
        } catch (final IllegalArgumentException | ArithmeticException ignored) {
        }

        throw new MoneyFormatException(value);
    }

    private Money(@Nonnull final Currency currency, @Nonnull final BigDecimal amount) {
        this.currency = currency;
        this.amount = amount;
    }

    /**
     * Gets the currency of this money.
     *
     * @return the currency, never missing
     */
    @Nonnull
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Gets the amount of this money.
     *
     * @return the amount, never missing
     */
    @Nonnull
    public BigDecimal getAmount() {
        return amount;
    }

    public Money convert(@Nonnull final Currency currency, @Nonnull final BigDecimal rate) {
        return new Money(currency, amount.multiply(rate));
    }

    public Money convert(@Nonnull final Function<Money, Money> rate) {
        return rate.apply(this);
    }

    public Money negate() {
        return new Money(currency, amount.negate());
    }

    public Money abs() {
        return new Money(currency, amount.abs());
    }

    public Money add(@Nonnull final Money that) {
        checkCurrency(that);
        return new Money(currency, amount.add(that.amount));
    }

    public Money subtract(@Nonnull final Money that) {
        checkCurrency(that);
        return new Money(currency, amount.subtract(that.amount));
    }

    public Money multiply(@Nonnull final BigDecimal rate) {
        return new Money(currency, amount.multiply(rate));
    }

    public Money divide(@Nonnull final BigDecimal rate) {
        return new Money(currency, amount.divide(rate));
    }

    public Money divide(@Nonnull final BigDecimal rate, @Nonnull final RoundingMode mode) {
        return new Money(currency, amount.divide(rate, mode));
    }

    public Money remainder(@Nonnull final BigDecimal rate) {
        return new Money(currency, amount.remainder(rate));
    }

    /**
     * Returns the ISO currency code followed by the amount with full decimal places.
     *
     * @return the text representation of this money, never missing
     */
    @Override
    @Nonnull
    public String toString() {
        return currency.getCurrencyCode() + amount;
    }

    /**
     * Formats in the current default {@link Locale.Category#FORMAT format} locale.
     *
     * @return the formatted money, never missing
     *
     * @see NumberFormat#getCurrencyInstance()
     */
    @Nonnull
    public String format() {
        final NumberFormat format = getCurrencyInstance();
        format.setCurrency(currency);
        return format.format(amount);
    }

    /**
     * Formats in the given <var>locale</var>.
     *
     * @param locale the locale guiding formatting, never missing
     *
     * @return the formatted money, never missing
     *
     * @see NumberFormat#getCurrencyInstance(Locale)
     */
    @Nonnull
    public String format(@Nonnull final Locale locale) {
        final NumberFormat format = getCurrencyInstance(locale);
        format.setCurrency(currency);
        return format.format(amount);
    }

    @Override
    public int compareTo(@Nonnull final Money that) {
        checkCurrency(that);
        return amount.compareTo(that.amount);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (null == o || getClass() != o.getClass())
            return false;

        final Money that = (Money) o;

        return currency.equals(that.currency) && amount.equals(that.amount);
    }

    @Override
    public int hashCode() {
        int result = currency.hashCode();
        result = 31 * result + amount.hashCode();
        return result;
    }

    private void checkCurrency(final Money that) {
        if (!currency.equals(that.currency))
            throw new IllegalArgumentException(
                    String.format("Different currencies: %s vs %s", currency, that.currency));
    }
}
