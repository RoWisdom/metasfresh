package org.compiere.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.adempiere.util.Check;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * This class contains the static methods and constants around {@link CtxName}.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
@UtilityClass
public class CtxNames
{
	public static final String NAME_Marker = "@";
	public static final String MODIFIER_Old = "old";

	public static final String VALUE_NULL = null;

	public static final String SEPARATOR = "/";
	private static final Splitter SEPARATOR_SPLITTER = Splitter.on(SEPARATOR)
	// .omitEmptyStrings() // DO NOT omit empty strings because we want to support expressions like: @Description/@
	;

	private static final List<String> MODIFIERS = ImmutableList.<String> builder()
			.add(MODIFIER_Old)
			.build();

	/**
	 * Returns an immutable set of {@link CtxName}s that contains the results of {@link CtxNames#parse(String)}, applied to the strings of the given {@code stringsWithoutMarkers}.
	 *
	 * @param stringsWithoutMarkers may not be {@code null}.
	 * @return
	 */
	public Set<CtxName> parseAll(@NonNull final Collection<String> stringsWithoutMarkers)
	{
		return stringsWithoutMarkers.stream()
				.map(CtxNames::parse)
				.collect(ImmutableSet.toImmutableSet());
	}

	/**
	 * Returns an immutable set of strings which contains the {@link CtxName#getName()}s of the give {@code ctxNames}.
	 *
	 * @param ctxNames may be {@code null}.
	 * @return
	 */
	public Set<String> toNames(@Nullable final Collection<CtxName> ctxNames)
	{
		if (ctxNames == null)
		{
			return null;
		}
		return ctxNames.stream()
				.map(CtxName::getName)
				.collect(ImmutableSet.toImmutableSet());
	}

	public static CtxName parse(final String contextWithoutMarkers)
	{
		if (contextWithoutMarkers == null)
		{
			return null;
		}

		final List<String> modifiers = new ArrayList<>();
		final String name = extractNameAndModifiers(contextWithoutMarkers, modifiers);

		final String defaultValue = extractDefaultValue(modifiers);

		return new CtxName(name, modifiers, defaultValue);
	}

	/**
	 * 
	 * @param contextWithoutMarkers
	 * @param modifiers found modifiers are added to this list
	 * @return
	 */
	private static String extractNameAndModifiers(
			@NonNull final String contextWithoutMarkers,
			@NonNull final List<String> modifiers)
	{
		String name = null;
		boolean firstToken = true;
		for (final String token : SEPARATOR_SPLITTER.splitToList(contextWithoutMarkers))
		{
			if (firstToken)
			{
				name = token.trim();
			}
			else
			{
				modifiers.add(token);
			}

			firstToken = false;
		}
		return name;
	}

	private static String extractDefaultValue(final List<String> modifiers)
	{
		final String defaultValue;
		if (!modifiers.isEmpty() && !isModifier(modifiers.get(modifiers.size() - 1)))
		{
			defaultValue = modifiers.remove(modifiers.size() - 1);
		}
		else
		{
			defaultValue = VALUE_NULL;
		}
		return defaultValue;
	}

	/** Parse a given name, surrounded by {@value #NAME_Marker} */
	public static CtxName parseWithMarkers(final String contextWithMarkers)
	{
		if (contextWithMarkers == null)
		{
			return null;
		}

		String contextWithoutMarkers = contextWithMarkers.trim();
		if (contextWithoutMarkers.startsWith(NAME_Marker))
		{
			contextWithoutMarkers = contextWithoutMarkers.substring(1);
		}
		if (contextWithoutMarkers.endsWith(NAME_Marker))
		{
			contextWithoutMarkers = contextWithoutMarkers.substring(0, contextWithoutMarkers.length() - 1);
		}

		return parse(contextWithoutMarkers);
	}

	/**
	 *
	 * @param name
	 * @param expression expression with context variables (e.g. sql where clause)
	 * @return true if expression contains given name
	 */
	@VisibleForTesting
	static boolean containsName(final String name, final String expression)
	{
		// FIXME: replace it with StringExpression
		if (name == null || name.isEmpty())
		{
			return false;
		}

		if (expression == null || expression.isEmpty())
		{
			return false;
		}

		final int idx = expression.indexOf(NAME_Marker + name);
		if (idx < 0)
		{
			return false;
		}

		final int idx2 = expression.indexOf(NAME_Marker, idx + 1);
		if (idx2 < 0)
		{
			return false;
		}

		final String nameStr = expression.substring(idx + 1, idx2);
		return name.equals(parse(nameStr).getName());
	}

	/**
	 *
	 * @param modifier
	 * @return true if given string is a registered modifier
	 */
	public static boolean isModifier(final String modifier)
	{
		if (Check.isEmpty(modifier))
		{
			return false;
		}
		return MODIFIERS.contains(modifier);
	}
}
