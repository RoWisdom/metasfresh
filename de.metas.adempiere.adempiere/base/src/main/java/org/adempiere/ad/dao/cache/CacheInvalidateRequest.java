package org.adempiere.ad.dao.cache;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.Check;
import org.adempiere.util.lang.impl.TableRecordReference;
import org.compiere.util.CacheMgt;

import lombok.NonNull;
import lombok.Value;

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

@Value
public final class CacheInvalidateRequest
{
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static CacheInvalidateRequest all()
	{
		return ALL;
	}

	public static CacheInvalidateRequest allRecordsForTable(@NonNull final String rootTableName)
	{
		final int rootRecordId = CacheMgt.RECORD_ID_ALL;
		final String childTableName = null;
		final int childRecordId = CacheMgt.RECORD_ID_ALL;
		return new CacheInvalidateRequest(rootTableName, rootRecordId, childTableName, childRecordId);
	}

	public static CacheInvalidateRequest rootRecord(@NonNull final String rootTableName, final int rootRecordId)
	{
		Check.assume(rootRecordId >= 0, "rootRecordId >= 0");
		
		final String childTableName = null;
		final int childRecordId = CacheMgt.RECORD_ID_ALL;
		return new CacheInvalidateRequest(rootTableName, rootRecordId, childTableName, childRecordId);
	}

	public static CacheInvalidateRequest allChildRecords(@NonNull final String rootTableName, final int rootRecordId, @NonNull final String childTableName)
	{
		Check.assume(rootRecordId >= 0, "rootRecordId >= 0");
		
		final int childRecordId = CacheMgt.RECORD_ID_ALL;
		return new CacheInvalidateRequest(rootTableName, rootRecordId, childTableName, childRecordId);
	}
	
	public static CacheInvalidateRequest fromTableNameAndRecordId(final String tableName, final int recordId)
	{
		if(tableName == null)
		{
			return all();
		}
		else if(recordId < 0)
		{
			return allRecordsForTable(tableName);
		}
		else
		{
			return rootRecord(tableName, recordId);
		}
	}

	private static final CacheInvalidateRequest ALL = new CacheInvalidateRequest(null, CacheMgt.RECORD_ID_ALL, null, CacheMgt.RECORD_ID_ALL);

	private final String rootTableName;
	private final int rootRecordId;

	private final String childTableName;
	private final int childRecordId;

	private CacheInvalidateRequest(
			final String rootTableName,
			final int rootRecordId,
			final String childTableName,
			final int childRecordId)
	{
		this.rootTableName = rootTableName;
		this.rootRecordId = rootRecordId >= 0 ? rootRecordId : CacheMgt.RECORD_ID_ALL;
		this.childTableName = childTableName;
		this.childRecordId = childRecordId >= 0 ? childRecordId : CacheMgt.RECORD_ID_ALL;
	}

	public boolean isAll()
	{
		return this == ALL;
	}
	
	public TableRecordReference getRootRecordOrNull()
	{
		if (rootTableName != null && rootRecordId >= 0)
		{
			return TableRecordReference.of(rootTableName, rootRecordId);
		}
		else
		{
			return null;
		}
	}

	public TableRecordReference getRecordEffective()
	{
		if (childTableName != null && childRecordId > 0)
		{
			return TableRecordReference.of(childTableName, childRecordId);
		}
		else if (rootTableName != null && rootRecordId > 0)
		{
			return TableRecordReference.of(rootTableName, rootRecordId);
		}
		else
		{
			throw new AdempiereException("Cannot extract effective record from " + this);
		}
	}
	
	public String getTableNameEffective()
	{
		return childTableName != null ? childTableName : rootTableName;
	}
	
	public int getRecordIdEffective()
	{
		return childTableName != null ? childRecordId : rootRecordId;
	}


	public static final class Builder
	{
		private String rootTableName;
		private int rootRecordId = -1;

		private String childTableName;
		private int childRecordId = -1;

		private Builder()
		{
		}

		public CacheInvalidateRequest build()
		{
			return new CacheInvalidateRequest(rootTableName, rootRecordId, childTableName, childRecordId);
		}

		public Builder rootRecord(@NonNull final String tableName, final int recordId)
		{
			Check.assume(recordId >= 0, "recordId >= 0");

			rootTableName = tableName;
			rootRecordId = recordId;
			return this;
		}

		public Builder childRecord(@NonNull final String tableName, final int recordId)
		{
			Check.assume(recordId >= 0, "recordId >= 0");

			childTableName = tableName;
			childRecordId = recordId;
			return this;
		}

	}
}
