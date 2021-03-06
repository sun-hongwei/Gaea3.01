USE [yx_mes]
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_role_data', @level2type=N'COLUMN',@level2name=N'roletype'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_role_data', @level2type=N'COLUMN',@level2name=N'opertype'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_role', @level2type=N'COLUMN',@level2name=N'roletype'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'roletype'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'cname'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'groupid'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'id'
GO
EXEC sys.sp_dropextendedproperty @name=N'MS_Description' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'cid'
GO
ALTER TABLE [dbo].[workflow_user] DROP CONSTRAINT [DF_workflow_user_lasttime]
GO
ALTER TABLE [dbo].[workflow_user] DROP CONSTRAINT [DF_workflow_user_registertime]
GO
ALTER TABLE [dbo].[workflow_user] DROP CONSTRAINT [DF_workflow_user_superdata]
GO
ALTER TABLE [dbo].[workflow_user] DROP CONSTRAINT [DF_workflow_user_supermenu]
GO
ALTER TABLE [dbo].[workflow_user] DROP CONSTRAINT [DF_workflow_user_superbutton]
GO
ALTER TABLE [dbo].[workflow_user] DROP CONSTRAINT [DF_workflow_user_superview]
GO
ALTER TABLE [dbo].[workflow_state] DROP CONSTRAINT [DF_workflow_state_count]
GO
ALTER TABLE [dbo].[workflow_run_task_do] DROP CONSTRAINT [DF_workflow_run_task_do_do_time]
GO
ALTER TABLE [dbo].[workflow_run_task_do] DROP CONSTRAINT [DF_workflow_run_task_do_task_state]
GO
ALTER TABLE [dbo].[workflow_run_task_do] DROP CONSTRAINT [DF_workflow_run_task_do_task_id]
GO
ALTER TABLE [dbo].[workflow_run_task_do] DROP CONSTRAINT [DF_workflow_run_task_do_id]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] DROP CONSTRAINT [DF_workflow_run_task_changes_change_time]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] DROP CONSTRAINT [DF_workflow_run_task_changes_do_time]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] DROP CONSTRAINT [DF_workflow_run_task_changes_task_id1]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] DROP CONSTRAINT [DF_workflow_run_task_changes_task_id]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] DROP CONSTRAINT [DF_workflow_run_task_changes_id]
GO
ALTER TABLE [dbo].[workflow_run_task] DROP CONSTRAINT [DF_workflow_run_task_create_time]
GO
ALTER TABLE [dbo].[workflow_run_task] DROP CONSTRAINT [DF_workflow_run_task_task_id]
GO
ALTER TABLE [dbo].[workflow_role_data] DROP CONSTRAINT [DF_workflow_role_data_roletype]
GO
ALTER TABLE [dbo].[workflow_jump_param] DROP CONSTRAINT [DF_workflow_param_lasttime]
GO
ALTER TABLE [dbo].[workflow_group_user] DROP CONSTRAINT [DF_workflow_group_detail_registertime]
GO
ALTER TABLE [dbo].[workflow_group] DROP CONSTRAINT [DF_workflow_group_grouptype]
GO
ALTER TABLE [dbo].[workflow_binduser] DROP CONSTRAINT [DF_lp_binduser_lp_binduser_status]
GO
/****** Object:  Index [IX_workflow_group_custom]    Script Date: 2019/7/29 16:10:30 ******/
DROP INDEX [IX_workflow_group_custom] ON [dbo].[workflow_group_custom]
GO
/****** Object:  Table [dbo].[workflow_user]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_user]
GO
/****** Object:  Table [dbo].[workflow_state]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_state]
GO
/****** Object:  Table [dbo].[workflow_run_task_do]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_run_task_do]
GO
/****** Object:  Table [dbo].[workflow_run_task_changes]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_run_task_changes]
GO
/****** Object:  Table [dbo].[workflow_run_task]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_run_task]
GO
/****** Object:  Table [dbo].[workflow_run]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_run]
GO
/****** Object:  Table [dbo].[workflow_role_data_group]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_role_data_group]
GO
/****** Object:  Table [dbo].[workflow_role_data]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_role_data]
GO
/****** Object:  Table [dbo].[workflow_role]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_role]
GO
/****** Object:  Table [dbo].[workflow_jump_param]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_jump_param]
GO
/****** Object:  Table [dbo].[workflow_group_user]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_group_user]
GO
/****** Object:  Table [dbo].[workflow_group_role]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_group_role]
GO
/****** Object:  Table [dbo].[workflow_group_custom_item]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_group_custom_item]
GO
/****** Object:  Table [dbo].[workflow_group_custom]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_group_custom]
GO
/****** Object:  Table [dbo].[workflow_group]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_group]
GO
/****** Object:  Table [dbo].[workflow_customrole]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_customrole]
GO
/****** Object:  Table [dbo].[workflow_binduser]    Script Date: 2019/7/29 16:10:30 ******/
DROP TABLE [dbo].[workflow_binduser]
GO
/****** Object:  Table [dbo].[workflow_binduser]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_binduser](
	[user_id] [bigint] IDENTITY(1,1) NOT NULL,
	[sn] [varchar](50) NULL,
	[addtime] [datetime] NULL,
	[type] [varchar](20) NULL,
	[status] [varchar](20) NULL,
 CONSTRAINT [PK_lp_binduser] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_customrole]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_customrole](
	[name] [varchar](100) NOT NULL,
	[tablename] [varchar](100) NOT NULL,
	[field] [varchar](100) NOT NULL,
	[usetype] [varchar](50) NOT NULL,
	[sqlinfo] [varchar](5000) NULL,
	[listinfo] [varchar](5000) NULL,
 CONSTRAINT [PK_workflow_customrole] PRIMARY KEY CLUSTERED 
(
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_group]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_group](
	[groupid] [varchar](255) NOT NULL,
	[grouppid] [varchar](255) NULL,
	[groupname] [varchar](255) NOT NULL,
	[grouptype] [varchar](128) NOT NULL,
	[groupmemo] [varchar](500) NULL,
 CONSTRAINT [PK_workflow_group] PRIMARY KEY CLUSTERED 
(
	[groupid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_group_custom]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_group_custom](
	[cid] [varchar](100) NOT NULL,
	[id] [varchar](100) NOT NULL,
	[groupid] [varchar](100) NOT NULL,
	[cname] [varchar](100) NOT NULL,
	[roletype] [varchar](50) NULL,
 CONSTRAINT [PK_workflow_group_custom] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_group_custom_item]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_group_custom_item](
	[cid] [varchar](100) NOT NULL,
	[item] [varchar](100) NOT NULL,
 CONSTRAINT [PK_workflow_group_custom_item] PRIMARY KEY CLUSTERED 
(
	[cid] ASC,
	[item] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_group_role]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_group_role](
	[groupid] [varchar](128) NOT NULL,
	[roleid] [varchar](128) NOT NULL,
	[roletype] [varchar](128) NOT NULL,
 CONSTRAINT [PK_workflow_group_role] PRIMARY KEY CLUSTERED 
(
	[groupid] ASC,
	[roleid] ASC,
	[roletype] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_group_user]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_group_user](
	[groupid] [varchar](128) NOT NULL,
	[userid] [varchar](128) NOT NULL,
	[updatetime] [datetime] NULL,
 CONSTRAINT [PK_workflow_group_detail] PRIMARY KEY CLUSTERED 
(
	[groupid] ASC,
	[userid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_jump_param]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_jump_param](
	[project] [varchar](128) NOT NULL,
	[workflow] [varchar](128) NOT NULL,
	[userid] [varchar](128) NOT NULL,
	[lasttime] [datetime] NOT NULL,
	[data] [text] NULL,
 CONSTRAINT [PK_workflow_param] PRIMARY KEY CLUSTERED 
(
	[project] ASC,
	[workflow] ASC,
	[userid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_role]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_role](
	[roleid] [varchar](128) NOT NULL,
	[roletext] [varchar](50) NOT NULL,
	[rolepid] [varchar](128) NULL,
	[rolememo] [varchar](500) NULL,
	[roletype] [varchar](50) NOT NULL,
 CONSTRAINT [PK_workflow_role] PRIMARY KEY CLUSTERED 
(
	[roleid] ASC,
	[roletype] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_role_data]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_role_data](
	[id] [varchar](128) NOT NULL,
	[groupid] [varchar](128) NOT NULL,
	[opertype] [varchar](50) NOT NULL,
	[roletype] [varchar](50) NOT NULL,
 CONSTRAINT [PK_workflow_role_data] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_role_data_group]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_role_data_group](
	[id] [varchar](128) NOT NULL,
	[groupid] [varchar](128) NOT NULL,
 CONSTRAINT [PK_workflow_role_data_group] PRIMARY KEY CLUSTERED 
(
	[id] ASC,
	[groupid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_run]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_run](
	[run_id] [varchar](255) NOT NULL,
	[run_name] [varchar](255) NOT NULL,
	[run_memo] [varchar](1000) NOT NULL,
	[run_editor_data] [text] NOT NULL,
 CONSTRAINT [PK_workflow_run] PRIMARY KEY CLUSTERED 
(
	[run_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_run_task]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_run_task](
	[task_id] [varchar](254) NOT NULL,
	[userid] [varchar](254) NOT NULL,
	[run_name] [varchar](254) NOT NULL,
	[task_memo] [varchar](254) NULL,
	[task_data] [text] NOT NULL,
	[create_time] [datetime] NOT NULL,
 CONSTRAINT [PK_workflow_run_task] PRIMARY KEY CLUSTERED 
(
	[task_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_run_task_changes]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_run_task_changes](
	[id] [varchar](255) NOT NULL,
	[task_id] [varchar](254) NOT NULL,
	[task_state] [varchar](254) NOT NULL,
	[userid] [varchar](254) NOT NULL,
	[do_time] [datetime] NOT NULL,
	[change_time] [datetime] NOT NULL,
 CONSTRAINT [PK_workflow_run_task_changes] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_run_task_do]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_run_task_do](
	[id] [varchar](255) NOT NULL,
	[task_id] [varchar](254) NOT NULL,
	[task_state] [varchar](254) NOT NULL,
	[userid] [varchar](254) NOT NULL,
	[do_time] [datetime] NOT NULL,
 CONSTRAINT [PK_workflow_run_task_do] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_state]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_state](
	[project] [varchar](400) NOT NULL,
	[item] [varchar](500) NOT NULL,
	[count] [int] NOT NULL,
 CONSTRAINT [PK_workflow_state] PRIMARY KEY CLUSTERED 
(
	[project] ASC,
	[item] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[workflow_user]    Script Date: 2019/7/29 16:10:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[workflow_user](
	[userid] [varchar](128) NOT NULL,
	[username] [varchar](50) NOT NULL,
	[password] [varchar](50) NOT NULL,
	[superview] [int] NOT NULL,
	[superbutton] [int] NOT NULL,
	[supermenu] [int] NOT NULL,
	[superdata] [int] NOT NULL,
	[registertime] [datetime] NOT NULL,
	[lasttime] [datetime] NOT NULL,
	[update_state] [varchar](50) NULL,
	[update_rowid] [varchar](50) NULL,
 CONSTRAINT [PK_workflow_user] PRIMARY KEY CLUSTERED 
(
	[userid] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [IX_workflow_group_custom]    Script Date: 2019/7/29 16:10:30 ******/
CREATE UNIQUE NONCLUSTERED INDEX [IX_workflow_group_custom] ON [dbo].[workflow_group_custom]
(
	[groupid] ASC,
	[cname] ASC,
	[roletype] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[workflow_binduser] ADD  CONSTRAINT [DF_lp_binduser_lp_binduser_status]  DEFAULT ('停用') FOR [status]
GO
ALTER TABLE [dbo].[workflow_group] ADD  CONSTRAINT [DF_workflow_group_grouptype]  DEFAULT ('role') FOR [grouptype]
GO
ALTER TABLE [dbo].[workflow_group_user] ADD  CONSTRAINT [DF_workflow_group_detail_registertime]  DEFAULT (getdate()) FOR [updatetime]
GO
ALTER TABLE [dbo].[workflow_jump_param] ADD  CONSTRAINT [DF_workflow_param_lasttime]  DEFAULT (getdate()) FOR [lasttime]
GO
ALTER TABLE [dbo].[workflow_role_data] ADD  CONSTRAINT [DF_workflow_role_data_roletype]  DEFAULT ('query') FOR [opertype]
GO
ALTER TABLE [dbo].[workflow_run_task] ADD  CONSTRAINT [DF_workflow_run_task_task_id]  DEFAULT (newid()) FOR [task_id]
GO
ALTER TABLE [dbo].[workflow_run_task] ADD  CONSTRAINT [DF_workflow_run_task_create_time]  DEFAULT (getdate()) FOR [create_time]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] ADD  CONSTRAINT [DF_workflow_run_task_changes_id]  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] ADD  CONSTRAINT [DF_workflow_run_task_changes_task_id]  DEFAULT (newid()) FOR [task_id]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] ADD  CONSTRAINT [DF_workflow_run_task_changes_task_id1]  DEFAULT (newid()) FOR [task_state]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] ADD  CONSTRAINT [DF_workflow_run_task_changes_do_time]  DEFAULT (getdate()) FOR [do_time]
GO
ALTER TABLE [dbo].[workflow_run_task_changes] ADD  CONSTRAINT [DF_workflow_run_task_changes_change_time]  DEFAULT (getdate()) FOR [change_time]
GO
ALTER TABLE [dbo].[workflow_run_task_do] ADD  CONSTRAINT [DF_workflow_run_task_do_id]  DEFAULT (newid()) FOR [id]
GO
ALTER TABLE [dbo].[workflow_run_task_do] ADD  CONSTRAINT [DF_workflow_run_task_do_task_id]  DEFAULT (newid()) FOR [task_id]
GO
ALTER TABLE [dbo].[workflow_run_task_do] ADD  CONSTRAINT [DF_workflow_run_task_do_task_state]  DEFAULT (newid()) FOR [task_state]
GO
ALTER TABLE [dbo].[workflow_run_task_do] ADD  CONSTRAINT [DF_workflow_run_task_do_do_time]  DEFAULT (getdate()) FOR [do_time]
GO
ALTER TABLE [dbo].[workflow_state] ADD  CONSTRAINT [DF_workflow_state_count]  DEFAULT ((0)) FOR [count]
GO
ALTER TABLE [dbo].[workflow_user] ADD  CONSTRAINT [DF_workflow_user_superview]  DEFAULT ((0)) FOR [superview]
GO
ALTER TABLE [dbo].[workflow_user] ADD  CONSTRAINT [DF_workflow_user_superbutton]  DEFAULT ((0)) FOR [superbutton]
GO
ALTER TABLE [dbo].[workflow_user] ADD  CONSTRAINT [DF_workflow_user_supermenu]  DEFAULT ((0)) FOR [supermenu]
GO
ALTER TABLE [dbo].[workflow_user] ADD  CONSTRAINT [DF_workflow_user_superdata]  DEFAULT ((0)) FOR [superdata]
GO
ALTER TABLE [dbo].[workflow_user] ADD  CONSTRAINT [DF_workflow_user_registertime]  DEFAULT (getdate()) FOR [registertime]
GO
ALTER TABLE [dbo].[workflow_user] ADD  CONSTRAINT [DF_workflow_user_lasttime]  DEFAULT (getdate()) FOR [lasttime]
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'唯一确定一组自定义权限，由于组可以重复，所以使用此值作为表示一组自定义权限的唯一索引' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'cid'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'主键及关联id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'id'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'组id' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'groupid'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'自定义数据规则名称' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'cname'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'数据权限类型，取值：[query,udpate]' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_group_custom', @level2type=N'COLUMN',@level2name=N'roletype'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'值(data：数据权限，func：功能权限，view：视图权限，button：按钮权限，menu：菜单权限，tree：导航树权限)' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_role', @level2type=N'COLUMN',@level2name=N'roletype'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'可以有2类权限，query：查询，update：更新,包括增、删、改' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_role_data', @level2type=N'COLUMN',@level2name=N'opertype'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'可选值为：self：仅自身数据，group：所在组数据，groups：多组数据' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'workflow_role_data', @level2type=N'COLUMN',@level2name=N'roletype'
GO
